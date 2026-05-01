package com.abhi1kush.positionanalyser.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	INVALID_REQUEST("POS-400-001", "Request validation failed", HttpStatus.BAD_REQUEST),
	INVALID_ANALYSIS_PROFILE("POS-400-002", "Invalid analysis profile", HttpStatus.BAD_REQUEST),
	INVALID_SIDE_FOCUS("POS-400-003", "Invalid side focus", HttpStatus.BAD_REQUEST),
	REVIEW_QUEUE_FULL("POS-429-001", "Review queue is full", HttpStatus.TOO_MANY_REQUESTS),
	REVIEW_RESULT_NOT_READY("POS-409-001", "Review result is not ready", HttpStatus.CONFLICT),
	REVIEW_JOB_NOT_FOUND("POS-404-001", "Review job not found", HttpStatus.NOT_FOUND),
	INTERNAL_ERROR("POS-500-001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String code;
	private final String description;
	private final HttpStatus httpStatus;

	ErrorCode(String code, String description, HttpStatus httpStatus) {
		this.code = code;
		this.description = description;
		this.httpStatus = httpStatus;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
