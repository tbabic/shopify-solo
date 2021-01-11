package org.bytepoet.shopifysolo.shopify.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyProduct {

	@JsonProperty
	private String id;
	
	@JsonProperty
	private List<Image> images;
	
	@JsonProperty
	private String status;
	
	@JsonProperty
	private String title;
	
	@JsonProperty
	private List<ShopifyProductVariant> variants;
	
	public static class Image {
		
		@JsonProperty
		private String id;
		
		@JsonProperty("product_id")
		private String productId;
	}

	
	
}
