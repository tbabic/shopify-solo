package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyDiscountAllocation {

	@JsonProperty("amount")
	private String amount;

	public String getAmount() {
		return amount;
	}
	
	
}
