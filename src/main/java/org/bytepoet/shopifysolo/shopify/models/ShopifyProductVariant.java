package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
	public Long quantity;
	
	@JsonProperty("grams")
	public String grams;
	
	@JsonProperty("inventory_item_id")
	public String inventoryItemId;
}
