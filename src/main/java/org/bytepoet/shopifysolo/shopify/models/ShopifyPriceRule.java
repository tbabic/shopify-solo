package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyPriceRule {

	@JsonProperty("price_rule")
	private PriceRule priceRule;
	
	public PriceRule getPriceRule() {
		return priceRule;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PriceRule {
		
		@JsonProperty 
		private String id;

		@JsonProperty("title")
		private String title;
		
		@JsonProperty("target_type")
		private String targetType;
		
		@JsonProperty("target_selection")
		private String targetSelection;
		
		@JsonProperty("allocation_method")
		private String allocationMethod;
		
		@JsonProperty("value_type") 
		private String valueType;
		
		@JsonProperty("value")
		private String value;
		
		@JsonProperty("customer_selection")
		private String cutomerSelection;
		
		@JsonProperty("starts_at")
		private String startsAt;
		
		@JsonProperty("once_per_customer")
		private boolean oncePerCustomer;
		
		@JsonProperty("usage_limit")
		private int usageLimit = 1;
		
		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getTargetType() {
			return targetType;
		}

		public String getTargetSelection() {
			return targetSelection;
		}

		public String getAllocationMethod() {
			return allocationMethod;
		}

		public String getValueType() {
			return valueType;
		}

		public String getValue() {
			return value;
		}

		public String getCutomerSelection() {
			return cutomerSelection;
		}

		public String getStartsAt() {
			return startsAt;
		}

		public boolean isOncePerCustomer() {
			return oncePerCustomer;
		}

		public int getUsageLimit() {
			return usageLimit;
		}
		
		public void increaseUsageLimit(int n) {
			this.usageLimit += n;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
		
		
	}
}
