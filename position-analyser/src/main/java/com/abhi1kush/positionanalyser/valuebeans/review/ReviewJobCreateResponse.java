package com.abhi1kush.positionanalyser.valuebeans.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewJobCreateResponse {

	private final String jobId;
	private final String status;
	private final long submittedAtEpochMs;
	private final long estimatedCompletionMs;
}
