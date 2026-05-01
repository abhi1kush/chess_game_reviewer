package com.abhi1kush.positionanalyser.valuebeans.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GameReviewSummaryVO {

	private final Integer accuracyWhite;
	private final Integer accuracyBlack;
	private final String openingName;
	private final List<Integer> criticalMoments;
}
