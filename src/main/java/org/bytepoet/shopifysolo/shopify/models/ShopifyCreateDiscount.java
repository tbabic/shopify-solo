package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyCreateDiscount {

	private DiscountCode discountCode;
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class DiscountCode {

		@JsonProperty
		private String code;
		
	}
	
	public ShopifyCreateDiscount(String code) {
		this.discountCode = new DiscountCode();
		this.discountCode.code = code;
	}
	
	
}
