package com.abhi1kush.positionanalyser.valuebeans.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewJobStatusResponse {

	private final String jobId;
	private final String status;
	private final int progressPct;
	private final String message;
	private final long submittedAtEpochMs;
	private final Long startedAtEpochMs;
	private final Long completedAtEpochMs;
}
