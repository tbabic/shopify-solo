package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyPricing {
	
	@JsonProperty("amount")
	private String amount;
	
	@JsonProperty("currency_code")
	private String currency;

	public String getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	@Override
	public String toString() {
		return "ShopifyPricing [amount=" + amount + ", currency=" + currency + "]";
	}
	
	
	
	
	

}
