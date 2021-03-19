package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyDiscountCode {

	@JsonProperty
	private String code;
	
	@JsonProperty
	private String type;
	
	@JsonProperty("amount")
	private String amount;

	public String getAmount() {
		return amount;
	}

	public String getCode() {
		return code;
	}

	public String getType() {
		return type;
	}
	
	
}
