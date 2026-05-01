package com.abhi1kush.positionanalyser.exception;

public class PositionAnalyserException extends RuntimeException {

	private final ErrorCode errorCode;
	private final String errorDescription;

	public PositionAnalyserException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
		this.errorDescription = errorCode.getDescription();
	}

	public PositionAnalyserException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
		this.errorDescription = errorCode.getDescription();
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}
}
