package org.bytepoet.shopifysolo.shopify.models;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyLineItem {

	@JsonProperty("title")
	private String title;
	
	@JsonProperty("quantity")
	private int quantity;
	
	@JsonProperty("price")
	private String price;
	
	@JsonProperty("variant_title")
	private String variantTitle;
	
	@JsonProperty("total_discount")
	private String totalDiscount;
	
	@JsonProperty("price_set")
	private ShopifyPriceSet priceSet;
	
	@JsonProperty("discount_allocations")
	private List<ShopifyDiscountAllocation> discountAllocations;

	public String getTitle() {
		return title;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getVariantTitle() {
		return variantTitle;
	}

	public String getTotalDiscount() {
		return totalDiscount;
	}
	
	@JsonIgnore
	public String getPricePerItem() {
		return priceSet.getPrice();
	}
	
	@JsonIgnore
	public String getCurrency() {
		return priceSet.getCurrency();
	}
	
	@JsonIgnore
	public String getDiscountPercent() {
		if (discountAllocations.isEmpty()) {
			return "0.00";
		}
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.HALF_UP);
		
		double pricePerItem = Double.parseDouble(getPricePerItem());
		double totalPrice = pricePerItem*quantity;
		double discountAmount = Double.parseDouble(discountAllocations.get(0).getAmount());
		double discount = 100* discountAmount / totalPrice;
		return df.format(discount);
	}

	@Override
	public String toString() {
		return "ShopifyLineItem [title=" + title + ", quantity=" + quantity + ", price=" + price + ", variantTitle="
				+ variantTitle + ", totalDiscount=" + totalDiscount + ", priceSet=" + priceSet + "]";
	}
	
	
	
	
}
