package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyInventoryAdjustment {

	
	@JsonProperty("location_id")
	public String locationId;
	
	@JsonProperty("inventory_item_id")
	public String inventoryItemId;
	
	@JsonProperty("available_adjustment")
	public int availableAdjustment;

}
