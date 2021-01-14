package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyUpdateVariantRequest {

	
	@JsonProperty
	private String price;
	
	@JsonProperty("compare_at_price")
	private String compareAtPrice;
	
}
