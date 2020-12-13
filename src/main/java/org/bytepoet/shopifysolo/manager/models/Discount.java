package org.bytepoet.shopifysolo.manager.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Discount {

	@JsonProperty
	private String code;
	
	@JsonProperty
	private String amount;

	public String getCode() {
		return code;
	}

	public String getAmount() {
		return amount;
	}	
	
}
