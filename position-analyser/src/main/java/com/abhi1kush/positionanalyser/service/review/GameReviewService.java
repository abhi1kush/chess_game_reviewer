package com.abhi1kush.positionanalyser.service.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abhi1kush.positionanalyser.service.engine.ChessEngineService;
import com.abhi1kush.positionanalyser.valuebeans.review.AnalysisProfile;
import com.abhi1kush.positionanalyser.valuebeans.review.GameReviewSummaryVO;
import com.abhi1kush.positionanalyser.valuebeans.review.MoveReviewVO;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewResultVO;
import com.abhi1kush.positionanalyser.valuebeans.review.SideFocus;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Constants;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

@Service
public class GameReviewService {

	public interface ProgressListener {
		void onProgress(int done, int total);
	}

	private final ChessEngineService engineService;
	private final FenSequenceBuilder fenSequenceBuilder;
	private final MoveClassifier moveClassifier;
	private final ReviewSummaryBuilder summaryBuilder;
	private final int quickDepth;
	private final int standardDepth;
	private final int deepDepth;

	public GameReviewService(ChessEngineService engineService, FenSequenceBuilder fenSequenceBuilder,
			MoveClassifier moveClassifier, ReviewSummaryBuilder summaryBuilder,
			@Value("${review.engine.depth.quick:8}") int quickDepth,
			@Value("${review.engine.depth.standard:12}") int standardDepth,
			@Value("${review.engine.depth.deep:16}") int deepDepth) {
		this.engineService = engineService;
		this.fenSequenceBuilder = fenSequenceBuilder;
		this.moveClassifier = moveClassifier;
		this.summaryBuilder = summaryBuilder;
		this.quickDepth = quickDepth;
		this.standardDepth = standardDepth;
		this.deepDepth = deepDepth;
	}

	public ReviewResultVO analyzeGame(String pgn, List<String> movesUci, AnalysisProfile profile, SideFocus sideFocus,
			boolean includePv, boolean includeFen, ProgressListener progressListener) {
		List<Move> moves = fenSequenceBuilder.parseMoves(pgn, movesUci);
		fenSequenceBuilder.validateMoveSequence(moves);

		int depth = depthFor(profile);
		List<MoveReviewVO> reviews = new ArrayList<>();
		Board board = new Board();
		board.loadFromFen(Constants.startStandardFENPosition);

		for (int i = 0; i < moves.size(); i++) {
			Move played = moves.get(i);
			Side sideToMove = board.getSideToMove();
			if (!shouldIncludeSide(sideFocus, sideToMove)) {
				board.doMove(played);
				if (progressListener != null) {
					progressListener.onProgress(i + 1, moves.size());
				}
				continue;
			}

			String fenBefore = board.getFen();
			int legalCount = legalMoveCount(board);
			ChessEngineService.EngineAnalysis evalBefore = engineService.analyze(fenBefore, depth, includePv);
			board.doMove(played);
			String fenAfter = board.getFen();
			ChessEngineService.EngineAnalysis evalAfter = engineService.analyze(fenAfter, depth, false);

			Integer evalBeforeCp = evalBefore != null ? evalBefore.getScoreCp() : null;
			Integer evalAfterCp = evalAfter != null ? evalAfter.getScoreCp() : null;
			Integer cpl = computeCpl(sideToMove, evalBeforeCp, evalAfterCp);

			List<String> tags = new ArrayList<>();
			if (legalCount <= 2) {
				tags.add("forced");
			}
			List<String> pv = includePv && evalBefore != null ? evalBefore.getPv() : Collections.<String>emptyList();
			String reviewFen = includeFen ? fenBefore : null;
			String classification = cpl != null ? moveClassifier.classify(cpl.intValue()) : "unknown";
			reviews.add(new MoveReviewVO(i + 1, sideToMove.name(), reviewFen, played.toString(),
					evalBefore != null ? evalBefore.getBestMoveUci() : null, evalBeforeCp, evalAfterCp, cpl,
					classification, legalCount, tags, pv));
			if (progressListener != null) {
				progressListener.onProgress(i + 1, moves.size());
			}
		}

		GameReviewSummaryVO summary = summaryBuilder.build(reviews);
		return new ReviewResultVO(profile, sideFocus, summary, reviews);
	}

	private boolean shouldIncludeSide(SideFocus sideFocus, Side sideToMove) {
		if (sideFocus == SideFocus.BOTH) {
			return true;
		}
		return (sideFocus == SideFocus.WHITE && sideToMove == Side.WHITE)
				|| (sideFocus == SideFocus.BLACK && sideToMove == Side.BLACK);
	}

	private int depthFor(AnalysisProfile profile) {
		switch (profile) {
			case QUICK:
				return quickDepth;
			case DEEP:
				return deepDepth;
			case STANDARD:
			default:
				return standardDepth;
		}
	}

	private Integer computeCpl(Side side, Integer evalBeforeCp, Integer evalAfterCp) {
		if (evalBeforeCp == null || evalAfterCp == null) {
			return null;
		}
		int delta;
		if (side == Side.WHITE) {
			delta = evalBeforeCp.intValue() - evalAfterCp.intValue();
		} else {
			delta = evalAfterCp.intValue() - evalBeforeCp.intValue();
		}
		return Integer.valueOf(Math.max(delta, 0));
	}

	private int legalMoveCount(Board board) {
		try {
			return MoveGenerator.generateLegalMoves(board).size();
		} catch (MoveGeneratorException e) {
			return 0;
		}
	}
}
