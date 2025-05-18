package com.codenavigator.code_navigator_api.dominio;

public enum SpringMethodAnnotationsEnum {

	GET("GetMapping", "GET"),
	POST("PostMapping", "POST"),
	PUT("PutMapping", "PUT"),
	PATCH("PatchMapping", "PATCH"),
	DELETE("DeleteMapping", "DELETE");
	
	public final String annotation;
	public final String value;
	
	private SpringMethodAnnotationsEnum(String annotation, String value) {
		this.annotation = annotation;
		this.value = value;
	}
}
