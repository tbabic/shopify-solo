package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyFulfillment {

	public String id;
	
	public String status;
	
	@JsonProperty("location_id")
	public String locationId;
}
