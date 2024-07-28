package com.warba.account.account_service.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.warba.account.account_service.util.BusinessException;


@ControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

	/*
	 * A simple exception handler to manage my custom exception.
	 */

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
		ex.printStackTrace();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", "BusinessException");
		map.put("code", ex.getCode());
		map.put("message", ex.getMessage());
		map.put("httpStatusCode", ex.getStatusCode());
		return handleExceptionInternal(ex, map, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
}