package org.bytepoet.shopifysolo.shopify.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyCreatePriceRule {

	@JsonProperty("price_rule")
	private PriceRule priceRule;
	
	public ShopifyCreatePriceRule(String title, String value) {
		this.priceRule = new PriceRule(title, value);
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class PriceRule {
		
		private PriceRule(String title, String value) {
			this.title = title;
			this.value = value;
			
			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			df.setTimeZone(tz);
			this.startsAt = df.format(new Date());
		}

		@JsonProperty("title")
		private String title;
		
		@JsonProperty("target_type")
		private String targetType = "line_item";
		
		@JsonProperty("target_selection")
		private String targetSelection = "all";
		
		@JsonProperty("allocation_method")
		private String allocationMethod = "across";
		
		@JsonProperty("value_type") 
		private String valueType = "fixed_amount";
		
		@JsonProperty("value")
		private String value;
		
		@JsonProperty("customer_selection")
		private String cutomerSelection = "all";
		
		@JsonProperty("starts_at")
		private String startsAt;
		
		@JsonProperty("once_per_customer")
		private boolean oncePerCustomer;
		
		@JsonProperty("usage_limit")
		private int usageLimit = 1;
		
	}

	
	
	
	
	
	
}
