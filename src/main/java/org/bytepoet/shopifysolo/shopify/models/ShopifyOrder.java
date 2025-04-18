package org.bytepoet.shopifysolo.shopify.models;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyOrder {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("order_number")
	private String number;
	
	@JsonProperty("payment_gateway_names")
	private List<String> gateways;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("note")
	private String note;
	
	@JsonProperty("total_price")
	private String totalPrice;
	
	@JsonProperty("tags")
	private String tags;
	
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
	
	@JsonProperty("discount_codes")
	private List<ShopifyDiscountCode> discountCodes;
	
	@JsonProperty("total_shipping_price_set")
	private ShopifyPriceSet shipping;
	
	@JsonProperty("shipping_address")
	private ShopifyShippingAddress shippingAddress;
	
	@JsonProperty("shipping_lines")
	private List<ShopifyShippingLine> shippingLines;
	
	@JsonProperty("note_attributes")
	private List<ShopifyNoteAttribute> noteAttributes;

	@JsonProperty("created_at")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
	private Date created;
	
	public String getId() {
		return id;
	}
	
	public String getNumber() {
		return number;
	}
	
	public List<String> getGateways() {
		return gateways;
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
	
	public String getTotalPrice() {
		return totalPrice;
	}
	
	public String getShippingTitle() {
		if (shippingLines == null || shippingLines.isEmpty()) {
			return null;
		}
		return shippingLines.get(0).getTitle();
	}
	
	public String getShippingCode() {
		if (shippingLines == null || shippingLines.isEmpty()) {
			return null;
		}
		return shippingLines.get(0).getCode();
	}

	public String getShippingPrice() {
		if (shippingLines == null || shippingLines.isEmpty()) {
			return "0.00";
		}
		return shippingLines.get(0).getPrice();
	}
	
	public Date getCreated() {
		return created;
	}
	
	public String getNote() {
		return note;
	}

	public String getTags() {
		return tags;
	}

	@JsonIgnore
	public String getTaxPercent() {
		double totalPrice = Double.parseDouble(this.totalPrice);
		double totalTax = Double.parseDouble(this.totalTax);
		String taxPercent = Long.toString(Math.round(100 * totalTax / totalPrice));
		return taxPercent;
	}
	
	public ShopifyShippingAddress getShippingAddress() {
		return shippingAddress;
	}
	
	@JsonProperty
	public String getFullAddress() {
		return shippingAddress.getFullAddress();
	}
	

	public List<ShopifyDiscountCode> getDiscountCodes() {
		return discountCodes;
	}

	@Override
	public String toString() {
		return "ShopifyOrder [id=" + id + ", email=" + email + ", totalPrice=" + totalPrice + ", subTotalPrice="
				+ subTotalPrice + ", totalTax=" + totalTax + ", taxesIncluded=" + taxesIncluded + ", currency="
				+ currency + ", customer=" + customer + ", lineItems=" + lineItems + "]";
	}
	
	public String getNoteAttributes() {
		return this.getNoteAttributes();
	}
	
	public String getNoteAttribute(String name) {
		
		if(this.noteAttributes == null || this.noteAttributes.isEmpty())
		{
			return null;
		}
		Optional<ShopifyNoteAttribute> found = this.noteAttributes.stream().filter(e -> name.equals(e.getName())).findFirst();
		if (!found.isPresent())
		{
			return null;
		}
		return found.get().getValue();
		
	}

}
