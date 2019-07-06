package org.bytepoet.shopifysolo.shopify.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyOrder {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("order_number")
	private String number;
	
	@JsonProperty("gateway")
	private String gateway;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("total_price")
	private String totalPrice;
	
	@JsonProperty("subtotal_price")
	private String subTotalPrice;
	
	@JsonProperty("total_tax")
	private String totalTax;
	
	@JsonProperty("taxes_included")
	private boolean taxesIncluded;
	
	@JsonProperty("currencyCode")
	private String currency;
	
	@JsonProperty("customer")
	private ShopifyCustomer customer;
	
	@JsonProperty("line_items")
	private List<ShopifyLineItem> lineItems;
	
	@JsonProperty("total_shipping_price_set")
	private ShopifyPriceSet shipping;
	
	@JsonProperty("shipping_address")
	private ShopifyShippingAddres shippingAddress;
	
	public String getId() {
		return id;
	}
	
	public String getNumber() {
		return number;
	}
	
	public String getGateway() {
		return gateway;
	}

	public String getEmail() {
		return email;
	}

	public boolean isTaxesIncluded() {
		return taxesIncluded;
	}

	public String getCurrency() {
		return currency;
	}

	public List<ShopifyLineItem> getLineItems() {
		return lineItems;
	}
	
	public String getShippingPrice() {
		return shipping.getPrice();
	}

	@JsonIgnore
	public String getTaxPercent() {
		double totalPrice = Double.parseDouble(this.totalPrice);
		double totalTax = Double.parseDouble(this.totalTax);
		String taxPercent = Long.toString(Math.round(100 * totalTax / totalPrice));
		return taxPercent;
	}

	@Override
	public String toString() {
		return "ShopifyOrder [id=" + id + ", email=" + email + ", totalPrice=" + totalPrice + ", subTotalPrice="
				+ subTotalPrice + ", totalTax=" + totalTax + ", taxesIncluded=" + taxesIncluded + ", currency="
				+ currency + ", customer=" + customer + ", lineItems=" + lineItems + "]";
	}
	
}
