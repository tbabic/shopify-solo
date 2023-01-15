package org.bytepoet.shopifysolo.shopify.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyProduct {

	@JsonProperty
	public String id;
	
	@JsonProperty
	public List<Image> images;
	
	@JsonProperty
	public String status;
	
	@JsonProperty
	public String title;
	
	@JsonProperty("body_html")
	public String bodyHtml;
	
	@JsonProperty
	public List<ShopifyProductVariant> variants;
	
	public static class Image {
		
		@JsonProperty
		private String id;
		
		@JsonProperty("product_id")
		private String productId;
	}

	
	
}
