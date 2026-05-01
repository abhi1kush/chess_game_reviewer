package com.abhi1kush.positionanalyser.valuebeans.review;

import com.abhi1kush.positionanalyser.exception.ErrorCode;
import com.abhi1kush.positionanalyser.exception.PositionAnalyserException;

public enum AnalysisProfile {
	QUICK,
	STANDARD,
	DEEP;

	public static AnalysisProfile fromNullable(String value) {
		if (value == null || value.trim().isEmpty()) {
			return STANDARD;
		}
		try {
			return AnalysisProfile.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new PositionAnalyserException(ErrorCode.INVALID_ANALYSIS_PROFILE,
					"Invalid analysisProfile. Use quick, standard, or deep.", ex);
		}
	}
}
