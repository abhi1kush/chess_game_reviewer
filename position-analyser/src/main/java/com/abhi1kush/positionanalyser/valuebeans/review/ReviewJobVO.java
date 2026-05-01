package com.abhi1kush.positionanalyser.valuebeans.review;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewJobVO {

	private final String id;
	private volatile ReviewJobStatus status;
	private volatile int progressPct;
	private volatile String message;
	private final long submittedAtEpochMs;
	private volatile Long startedAtEpochMs;
	private volatile Long completedAtEpochMs;
	private final AnalysisProfile analysisProfile;
	private final SideFocus sideFocus;
	private volatile ReviewResultVO result;
	private volatile String error;

	public ReviewJobVO(String id, AnalysisProfile analysisProfile, SideFocus sideFocus) {
		this.id = id;
		this.analysisProfile = analysisProfile;
		this.sideFocus = sideFocus;
		this.status = ReviewJobStatus.QUEUED;
		this.progressPct = 0;
		this.message = "Queued";
		this.submittedAtEpochMs = System.currentTimeMillis();
	}
}
