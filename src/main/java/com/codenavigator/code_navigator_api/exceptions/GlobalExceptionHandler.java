package com.codenavigator.code_navigator_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(InfrastructureException.class)
	public ResponseEntity<String> handleInfrastructure(InfrastructureException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Technical error: ".concat(ex.getMessage()));
	}
	
}
