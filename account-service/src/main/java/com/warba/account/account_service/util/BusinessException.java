package com.warba.account.account_service.util;

public class BusinessException extends RuntimeException {

	/*
	 * This class is a custom exception that is used for our custom exceptions
	 */
	private static final long serialVersionUID = 1L;

	private String code;
	private int statusCode;
	String message;
	// we can add other fields depends on the need

	public BusinessException(String stackTrace) {
		super(stackTrace);
	}

	public BusinessException(String code, String stackTrace, int statusCode) {
		super(stackTrace);
		this.code = code;
		this.statusCode = statusCode;
		this.message = stackTrace;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
