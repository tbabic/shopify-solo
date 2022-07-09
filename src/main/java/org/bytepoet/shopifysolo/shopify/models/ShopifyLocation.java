package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopifyLocation {

	@JsonProperty
	public String id;
	
	@JsonProperty
	public String name;
	
	@JsonProperty
	public String address1;
	
	@JsonProperty
	public String address2;
	
	@JsonProperty
	public String city;
	
	@JsonProperty
	public String zip;
	
	@JsonProperty("country_name")
	public String countryName;
}
