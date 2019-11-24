package org.bytepoet.shopifysolo.print.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Base64Wrapper {

	@JsonProperty
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
