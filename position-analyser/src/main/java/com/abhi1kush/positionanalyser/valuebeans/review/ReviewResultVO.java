package com.abhi1kush.positionanalyser.valuebeans.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResultVO {

	private final AnalysisProfile analysisProfile;
	private final SideFocus sideFocus;
	private final GameReviewSummaryVO summary;
	private final List<MoveReviewVO> moveReviews;
}
