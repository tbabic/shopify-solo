package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyShippingAddress {

	@JsonProperty("name")
	private String name;

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
    
    @JsonIgnore
    public String getFullAddress() {
    	return new StringBuilder()
    			.append(name)
    			.append("\n")
    			.append(address1)
    			.append("\n")
    			.append(zip)
    			.append(" ")
    			.append(city)
    			.append("\n")
    			.append(country)
    			.toString();
    }

	public String getName() {
		return name;
	}

	public String getAddress1() {
		return address1;
	}

	public String getAddress2() {
		return address2;
	}

	public String getPhone() {
		return phone;
	}

	public String getCity() {
		return city;
	}

	public String getZip() {
		return zip;
	}

	public String getProvince() {
		return province;
	}

	public String getCountry() {
		return country;
	}

	public String getCompany() {
		return company;
	}
    
    
}
