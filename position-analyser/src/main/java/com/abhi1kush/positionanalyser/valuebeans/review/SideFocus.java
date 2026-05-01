package com.abhi1kush.positionanalyser.valuebeans.review;

import com.abhi1kush.positionanalyser.exception.ErrorCode;
import com.abhi1kush.positionanalyser.exception.PositionAnalyserException;

public enum SideFocus {
	WHITE,
	BLACK,
	BOTH;

	public static SideFocus fromNullable(String value) {
		if (value == null || value.trim().isEmpty()) {
			return BOTH;
		}
		try {
			return SideFocus.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new PositionAnalyserException(ErrorCode.INVALID_SIDE_FOCUS,
					"Invalid sideFocus. Use white, black, or both.", ex);
		}
	}
}
