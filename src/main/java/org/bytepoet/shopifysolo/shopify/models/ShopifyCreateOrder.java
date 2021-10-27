package org.bytepoet.shopifysolo.shopify.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyCreateOrder {
	
	@JsonProperty
	private String email;
	
	@JsonProperty("shipping_address")
	private ShippingAddress shippingAddress;
	
	@JsonProperty("line_items")
	private List<LineItem> lineItems;
	
	@JsonProperty("tags")
	private String tags ="giveaway";
	
	@JsonProperty("inventory_behaviour")
	private String inventoryBehaviour = "decrement_ignoring_policy";
	
	@JsonProperty("send_receipt")
	private boolean sendReceipt = false;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class LineItem {
		
		@JsonProperty("variant_id")
		private String variantId;
		
		@JsonProperty
		private String title;
		
		@JsonProperty
		private String price;
		
		@JsonProperty
		private long quantity;
		
		@JsonProperty("applied_discount")
		private Discount appliedDiscount;	
		
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Discount {

		@JsonProperty("value")
		private String value;
		
		@JsonProperty("value_type")
		private String type;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ShippingAddress {
		
		
		@JsonProperty("last_name")
		private String lastName;
		
		@JsonProperty("first_name")
		private String firstName;

	    @JsonProperty("address1")
		private String address1;
	    
	    @JsonProperty("address2")
		private String address2;

	    @JsonProperty("phone")
		private String phone;
	    
	    @JsonProperty("city")
		private String city;
	    
	    @JsonProperty("zip")
		private String zip;

	    @JsonProperty("province")
		private String province;
	    
	    @JsonProperty("country")
		private String country;   
	    
	    @JsonProperty("company")
		private String company;	    
		
		
	}
}
