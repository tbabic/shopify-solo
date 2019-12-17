package org.bytepoet.shopifysolo.manager.models;

import javax.persistence.Embeddable;

import org.bytepoet.shopifysolo.shopify.models.ShopifyShippingAddress;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class Address {

	public Address() {}
	
	public Address(ShopifyShippingAddress shopifyAddress) {
		fullName = shopifyAddress.getName();
		companyName = shopifyAddress.getCompany();
		streetAndNumber = shopifyAddress.getAddress1();
		other = shopifyAddress.getAddress2();
		city = shopifyAddress.getCity();
		postalCode = shopifyAddress.getZip();
		country = shopifyAddress.getCountry();
		phoneNumber = shopifyAddress.getPhone();
	}
	
	@JsonProperty
	private String fullName;
	@JsonProperty
	private String companyName;
	@JsonProperty
	private String streetAndNumber;
	@JsonProperty
	private String other;
	@JsonProperty
	private String city;
	@JsonProperty
	private String postalCode;
	@JsonProperty
	private String country;
	@JsonProperty
	private String phoneNumber;
}
