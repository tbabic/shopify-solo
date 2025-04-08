package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyNoteAttribute {

	@JsonProperty("name")
	private String name;
	
	@JsonProperty("value")
	private String value;

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	
}
