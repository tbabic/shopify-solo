package org.bytepoet.shopifysolo.shopify.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyShippingLine {

	@JsonProperty("title")
	private String title;
	
	@JsonProperty
	private String price;
	
	@JsonProperty("tax_lines")
	private List<ShopifyTaxLine> taxLines;
	
	
	
	public String getTitle() {
		return title;
	}

	public String getPrice() {
		return price;
	}

	public String getTaxAmount() {
		if (taxLines == null || taxLines.isEmpty()) {
			return "0";
		}
		return taxLines.get(0).getPrice();
	}
	
	public String getTaxRate() {
		if (taxLines == null || taxLines.isEmpty()) {
			return "0";
		}
		return taxLines.get(0).getRate();
	}
}
