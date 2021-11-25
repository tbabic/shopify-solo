package org.bytepoet.shopifysolo.shopify.models;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyLineItem {

	@JsonProperty("id")
	private String id;
	
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
	
	@JsonProperty("variant_id")
	private String variantId;
	
	@JsonProperty("grams")
	private double grams;

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
	
	public double getGrams() {
		return grams;
	}
	
	public String getId() {
		return id;
	}

	public String getFullTitle() {
		if (StringUtils.isBlank(variantTitle)) {
			return title;
		}
		return title + "/" + variantTitle;
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
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(newSymbols);
		
		
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

	public String getVariantId() {
		return variantId;
	}
	
	
	
	
	
	
}
