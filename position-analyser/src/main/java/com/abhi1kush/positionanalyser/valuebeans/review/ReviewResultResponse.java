package com.abhi1kush.positionanalyser.valuebeans.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResultResponse {

	private final String jobId;
	private final String status;
	private final AnalysisProfile analysisProfile;
	private final SideFocus sideFocus;
	private final GameReviewSummaryResponse summary;
	private final List<MoveReviewResponse> moves;
}
