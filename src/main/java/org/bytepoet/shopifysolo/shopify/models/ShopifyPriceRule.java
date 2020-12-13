package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyPriceRule {

	@JsonProperty("price_rule")
	private PriceRule priceRule;
	
	public PriceRule getPriceRule() {
		return priceRule;
	}
	
	public static class PriceRule {
		
		@JsonProperty 
		private String id;

		public String getId() {
			return id;
		}
		
	}
}
