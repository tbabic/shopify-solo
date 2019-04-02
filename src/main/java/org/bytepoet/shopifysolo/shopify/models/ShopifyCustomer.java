package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyCustomer {

	@JsonProperty("email")
	private String email;
	
	@JsonProperty("phone")
	private String phone;

	@Override
	public String toString() {
		return "ShopifyCustomer [email=" + email + ", phone=" + phone + "]";
	}
	
	
}
