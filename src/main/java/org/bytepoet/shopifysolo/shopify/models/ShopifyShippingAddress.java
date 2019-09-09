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
}
