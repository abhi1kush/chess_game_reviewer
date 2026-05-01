package com.abhi1kush.positionanalyser.service.review;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abhi1kush.positionanalyser.dao.review.ReviewJobDao;
import com.abhi1kush.positionanalyser.exception.ErrorCode;
import com.abhi1kush.positionanalyser.exception.PositionAnalyserException;
import com.abhi1kush.positionanalyser.valuebeans.review.AnalysisProfile;
import com.abhi1kush.positionanalyser.valuebeans.review.GameReviewSummaryResponse;
import com.abhi1kush.positionanalyser.valuebeans.review.GameReviewSummaryVO;
import com.abhi1kush.positionanalyser.valuebeans.review.MoveReviewVO;
import com.abhi1kush.positionanalyser.valuebeans.review.MoveReviewResponse;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobStatus;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobCreateRequest;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobCreateResponse;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobVO;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobStatusResponse;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewResultVO;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewResultResponse;
import com.abhi1kush.positionanalyser.valuebeans.review.SideFocus;

@Service
public class GameReviewJobService {

	private final GameReviewService gameReviewService;
	private final ReviewJobDao reviewJobDao;
	private final ExecutorService executor;
	private final int maxQueueSize;

	public GameReviewJobService(GameReviewService gameReviewService, ReviewJobDao reviewJobDao,
			@Value("${review.job.maxConcurrent:2}") int maxConcurrent,
			@Value("${review.job.maxQueueSize:100}") int maxQueueSize) {
		this.gameReviewService = gameReviewService;
		this.reviewJobDao = reviewJobDao;
		this.executor = Executors.newFixedThreadPool(Math.max(1, maxConcurrent));
		this.maxQueueSize = Math.max(1, maxQueueSize);
	}

	public ReviewJobCreateResponse submit(ReviewJobCreateRequest request) {
		long outstanding = reviewJobDao.findAll().stream()
				.filter(job -> job.getStatus() == ReviewJobStatus.QUEUED || job.getStatus() == ReviewJobStatus.RUNNING)
				.count();
		if (outstanding >= maxQueueSize) {
			throw new PositionAnalyserException(ErrorCode.REVIEW_QUEUE_FULL,
					"Review queue is full. Please retry in a moment.");
		}
		AnalysisProfile profile = AnalysisProfile.fromNullable(request.getAnalysisProfile());
		SideFocus sideFocus = SideFocus.fromNullable(request.getSideFocus());
		String jobId = UUID.randomUUID().toString();
		ReviewJobVO job = new ReviewJobVO(jobId, profile, sideFocus);
		reviewJobDao.save(job);

		executor.submit(() -> runJob(job, request));
		long estimate = estimateCompletionMs(request, profile);
		return new ReviewJobCreateResponse(jobId, job.getStatus().name().toLowerCase(), job.getSubmittedAtEpochMs(), estimate);
	}

	public ReviewJobStatusResponse getStatus(String jobId) {
		ReviewJobVO job = requireJob(jobId);
		String message = job.getError() != null ? job.getError() : job.getMessage();
		return new ReviewJobStatusResponse(job.getId(), job.getStatus().name().toLowerCase(), job.getProgressPct(), message,
				job.getSubmittedAtEpochMs(), job.getStartedAtEpochMs(), job.getCompletedAtEpochMs());
	}

	public ReviewResultResponse getResult(String jobId, boolean includePv, boolean includeFen) {
		ReviewJobVO job = requireJob(jobId);
		if (job.getStatus() != ReviewJobStatus.COMPLETED) {
			throw new PositionAnalyserException(ErrorCode.REVIEW_RESULT_NOT_READY,
					"Review result not ready. Current status: " + job.getStatus().name().toLowerCase());
		}
		ReviewResultVO result = job.getResult();
		List<MoveReviewResponse> moveResponses = new ArrayList<>();
		for (MoveReviewVO move : result.getMoveReviews()) {
			moveResponses.add(new MoveReviewResponse(
					move.getPly(),
					move.getSide(),
					includeFen ? move.getFenBefore() : null,
					move.getPlayedMoveUci(),
					move.getBestMoveUci(),
					move.getEvalBeforeCp(),
					move.getEvalAfterCp(),
					move.getCpl(),
					move.getClassification(),
					move.getLegalMoveCount(),
					move.getTags(),
					includePv ? move.getPv() : null));
		}
		GameReviewSummaryVO summary = result.getSummary();
		GameReviewSummaryResponse summaryResponse = new GameReviewSummaryResponse(summary.getAccuracyWhite(),
				summary.getAccuracyBlack(), summary.getOpeningName(), summary.getCriticalMoments());
		return new ReviewResultResponse(job.getId(), job.getStatus().name().toLowerCase(), result.getAnalysisProfile(),
				result.getSideFocus(), summaryResponse, moveResponses);
	}

	public ReviewResultResponse quickReview(ReviewJobCreateRequest request) {
		AnalysisProfile profile = AnalysisProfile.QUICK;
		SideFocus sideFocus = SideFocus.fromNullable(request.getSideFocus());
		ReviewResultVO result = gameReviewService.analyzeGame(request.getPgn(), request.getMovesUci(), profile, sideFocus, false, false,
				null);
		GameReviewSummaryVO summary = result.getSummary();
		GameReviewSummaryResponse summaryResponse = new GameReviewSummaryResponse(summary.getAccuracyWhite(),
				summary.getAccuracyBlack(), summary.getOpeningName(), summary.getCriticalMoments());
		List<MoveReviewResponse> moveResponses = new ArrayList<>();
		for (MoveReviewVO move : result.getMoveReviews()) {
			moveResponses.add(new MoveReviewResponse(move.getPly(), move.getSide(), null, move.getPlayedMoveUci(),
					move.getBestMoveUci(), move.getEvalBeforeCp(), move.getEvalAfterCp(), move.getCpl(),
					move.getClassification(), move.getLegalMoveCount(), move.getTags(), null));
		}
		return new ReviewResultResponse("quick", "completed", profile, sideFocus, summaryResponse, moveResponses);
	}

	private void runJob(ReviewJobVO job, ReviewJobCreateRequest request) {
		job.setStatus(ReviewJobStatus.RUNNING);
		job.setMessage("Running analysis");
		job.setStartedAtEpochMs(System.currentTimeMillis());
		try {
			ReviewResultVO result = gameReviewService.analyzeGame(request.getPgn(), request.getMovesUci(), job.getAnalysisProfile(),
					job.getSideFocus(), true, true, (done, total) -> {
						int progress = total == 0 ? 0 : (done * 100) / total;
						job.setProgressPct(progress);
						job.setMessage("Processed " + done + "/" + total + " plies");
					});
			job.setResult(result);
			job.setStatus(ReviewJobStatus.COMPLETED);
			job.setProgressPct(100);
			job.setMessage("Completed");
		} catch (RuntimeException ex) {
			job.setStatus(ReviewJobStatus.FAILED);
			job.setError(ex.getMessage());
			job.setMessage("Failed");
		} finally {
			job.setCompletedAtEpochMs(System.currentTimeMillis());
		}
	}

	private long estimateCompletionMs(ReviewJobCreateRequest request, AnalysisProfile profile) {
		int moves = 40;
		if (request.getMovesUci() != null && !request.getMovesUci().isEmpty()) {
			moves = request.getMovesUci().size();
		}
		int perMoveMs;
		switch (profile) {
			case QUICK:
				perMoveMs = 250;
				break;
			case DEEP:
				perMoveMs = 900;
				break;
			case STANDARD:
			default:
				perMoveMs = 500;
				break;
		}
		return (long) moves * perMoveMs;
	}

	private ReviewJobVO requireJob(String jobId) {
		ReviewJobVO job = reviewJobDao.findById(jobId);
		if (job == null) {
			throw new PositionAnalyserException(ErrorCode.REVIEW_JOB_NOT_FOUND, "Unknown review jobId: " + jobId);
		}
		return job;
	}

	@PreDestroy
	public void shutdown() {
		executor.shutdownNow();
	}
}
