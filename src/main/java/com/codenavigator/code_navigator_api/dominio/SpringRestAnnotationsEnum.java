package com.codenavigator.code_navigator_api.dominio;

public enum SpringRestAnnotationsEnum {

	REST_CONTROLLER("RestController", "Rest Controller"),
	REQUEST_MAPPING("RequestMapping", "Request Mapping");
	
	
	public final String annotation;
	public final String value;
	
	private SpringRestAnnotationsEnum(String annotation, String value) {
		this.annotation = annotation;
		this.value = value;
	}
}
