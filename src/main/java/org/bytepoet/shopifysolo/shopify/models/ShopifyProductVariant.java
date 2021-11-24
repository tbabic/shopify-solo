package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyProductVariant {

	@JsonProperty
	public String id;
	
	@JsonProperty
	public String title;
	
	@JsonProperty
	public String price;
	
	@JsonProperty("compare_at_price")
	public String compareAtPrice;
	
	@JsonProperty("inventory_quantity")
	public String quantity;
}
