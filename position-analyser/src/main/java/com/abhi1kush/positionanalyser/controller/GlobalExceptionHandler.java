package com.abhi1kush.positionanalyser.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.abhi1kush.positionanalyser.exception.ErrorCode;
import com.abhi1kush.positionanalyser.exception.ErrorResponse;
import com.abhi1kush.positionanalyser.exception.PositionAnalyserException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(PositionAnalyserException.class)
	public ResponseEntity<ErrorResponse> handlePositionAnalyserException(PositionAnalyserException e, HttpServletRequest request) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("Business exception code={} description={} message={} path={}",
				errorCode.getCode(), e.getErrorDescription(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(errorCode.getHttpStatus())
				.body(ErrorResponse.builder()
						.errorCode(errorCode.getCode())
						.errorDescription(e.getErrorDescription())
						.message(e.getMessage())
						.path(request.getRequestURI())
						.timestamp(System.currentTimeMillis())
						.build());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e, HttpServletRequest request) {
		String msg = e.getMessage() != null ? e.getMessage() : "Bad request";
		log.warn("Validation exception message={} path={}", msg, request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.builder()
						.errorCode(ErrorCode.INVALID_REQUEST.getCode())
						.errorDescription(ErrorCode.INVALID_REQUEST.getDescription())
						.message(msg)
						.path(request.getRequestURI())
						.timestamp(System.currentTimeMillis())
						.build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
		log.error("Unhandled exception path={}", request.getRequestURI(), e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.builder()
						.errorCode(ErrorCode.INTERNAL_ERROR.getCode())
						.errorDescription(ErrorCode.INTERNAL_ERROR.getDescription())
						.message("Unexpected error occurred")
						.path(request.getRequestURI())
						.timestamp(System.currentTimeMillis())
						.build());
	}
}
