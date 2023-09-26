package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyTaxLine {

	@JsonProperty
	private String price;
	
	@JsonProperty
	private String rate;

	public String getPrice() {
		return price;
	}

	public String getRate() {
		return rate;
	}
	
	
}
