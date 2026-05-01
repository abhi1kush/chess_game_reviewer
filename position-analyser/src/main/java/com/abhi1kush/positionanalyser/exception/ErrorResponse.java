package com.abhi1kush.positionanalyser.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
	private final String errorCode;
	private final String errorDescription;
	private final String message;
	private final String path;
	private final long timestamp;
}
