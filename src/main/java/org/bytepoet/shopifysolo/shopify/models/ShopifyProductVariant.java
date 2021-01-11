package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyProductVariant {

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String title;
	
	@JsonProperty
	private String price;
}
