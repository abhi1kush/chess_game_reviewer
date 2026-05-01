package com.abhi1kush.positionanalyser.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abhi1kush.positionanalyser.service.review.GameReviewJobService;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobCreateRequest;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobCreateResponse;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobStatusResponse;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewResultResponse;

@RestController
@RequestMapping("/api/review")
public class ChessReviewController {

	private final GameReviewJobService gameReviewJobService;

	public ChessReviewController(GameReviewJobService gameReviewJobService) {
		this.gameReviewJobService = gameReviewJobService;
	}

	@PostMapping("/jobs")
	public ReviewJobCreateResponse createJob(@RequestBody ReviewJobCreateRequest request) {
		return gameReviewJobService.submit(request);
	}

	@GetMapping("/jobs/{jobId}")
	public ReviewJobStatusResponse status(@PathVariable String jobId) {
		return gameReviewJobService.getStatus(jobId);
	}

	@GetMapping("/jobs/{jobId}/result")
	public ReviewResultResponse result(@PathVariable String jobId,
			@RequestParam(defaultValue = "true") boolean includePv,
			@RequestParam(defaultValue = "true") boolean includeFen) {
		return gameReviewJobService.getResult(jobId, includePv, includeFen);
	}

	@PostMapping("/quick")
	public ReviewResultResponse quick(@RequestBody ReviewJobCreateRequest request) {
		return gameReviewJobService.quickReview(request);
	}
}
