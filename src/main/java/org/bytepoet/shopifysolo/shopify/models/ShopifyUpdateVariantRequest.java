package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyUpdateVariantRequest {

	
	@JsonProperty
	private String price;
	
	@JsonProperty("compare_at_price")
	private String compareAtPrice;
	
	public static ShopifyUpdateVariantRequest create(String price, String compareAtPrice) {
		ShopifyUpdateVariantRequest request = new ShopifyUpdateVariantRequest();
		request.price = price;
		request.compareAtPrice = compareAtPrice;
		return request;
	}
	
}
