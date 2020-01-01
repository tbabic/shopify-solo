package org.bytepoet.shopifysolo.manager.models;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.print.models.PostalFormAddress;
import org.bytepoet.shopifysolo.shopify.models.ShopifyShippingAddress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class Address implements PostalFormAddress{

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
	
	@JsonIgnore
	@Transient
	public String getFullRecepient() {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(companyName)) {
			sb.append(companyName);
		}
		if (StringUtils.isNoneBlank(companyName, fullName)) {
			sb.append(" n/r ");
		}
		if (StringUtils.isNotBlank(fullName)) {
			sb.append(fullName);
		}
		return sb.toString();
	}
	
	@JsonIgnore
	@Transient
	public String getFullAddress() {
		StringBuilder sb = new StringBuilder(streetAndNumber);
		if (StringUtils.isNotBlank(other)) {
			sb.append(", ");
			sb.append(other);
		}
		if (!StringUtils.equalsIgnoreCase(country, "Croatia")) {
			sb.append(", ");
			sb.append(country);
		}
		return sb.toString();
	}

	@JsonIgnore
	@Transient
	public String getFullDestination() {
		StringBuilder sb = new StringBuilder(postalCode);
		sb.append(", ");
		sb.append(city);
		return sb.toString();
	}
}
