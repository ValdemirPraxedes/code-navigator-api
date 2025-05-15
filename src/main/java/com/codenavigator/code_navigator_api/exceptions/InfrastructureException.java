package com.codenavigator.code_navigator_api.exceptions;

public class InfrastructureException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public InfrastructureException(String message) {
		super(message);
	}
	
	public InfrastructureException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InfrastructureException(Throwable cause) {
		super(cause);
	}

}
