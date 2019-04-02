package org.bytepoet.shopifysolo.shopify.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyPriceSet {

	@JsonProperty("shop_money")
	private ShopifyPricing pricing;
	
	@JsonProperty("discount_allocations")
	private List<Object> discountAllocations;
	
	@JsonProperty("tax_lines")
	private List<Object> taxes;
	
	@JsonIgnore
	public String getPrice() {
		return pricing.getAmount();
	}
	
	@JsonIgnore
	public String getCurrency() {
		return pricing.getCurrency();
	}

	public ShopifyPricing getPricing() {
		return pricing;
	}

	@Override
	public String toString() {
		return "ShopifyPriceSet [pricing=" + pricing + ", discountAllocations=" + discountAllocations + ", taxes="
				+ taxes + "]";
	}
	
	
	
	
}
