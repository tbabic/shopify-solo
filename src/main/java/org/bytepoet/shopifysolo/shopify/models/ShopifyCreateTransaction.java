package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyCreateTransaction {

	@JsonProperty("transaction")
	private Transaction transaction;
	
	public ShopifyCreateTransaction(String currency, String amount, String kind, String parentId) {
		this.transaction = new Transaction(currency, amount, kind, parentId);
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Transaction {
		
		@JsonProperty("currency")
		private String currency;
		
		@JsonProperty("amount")
		private String amount;
		
		@JsonProperty("kind")
		private String kind;
		
		@JsonProperty("parent_id")
		private String parentId;
		
		@JsonProperty("source")
		private String source = "external";

		private Transaction(String currency, String amount, String kind, String parentId) {
			this.currency = currency;
			this.amount = amount;
			this.kind = kind;
			this.parentId = parentId;
		}
		
		
	}
}
