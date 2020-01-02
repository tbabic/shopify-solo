package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyTransaction {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("order_id")
	private String orderId;
	
	@JsonProperty("kind")
	private String kind;
	
	@JsonProperty("gateway")
	private String gateway;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("created_at")
	private String createdAt;
	
	@JsonProperty("test")
	private Boolean test;
	
	@JsonProperty("authorization")
	private String authorization;
	
	@JsonProperty("location_id")
	private String locationId;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("parent_id")
	private String parentId;
	
	@JsonProperty("processed_at")
	private String processedAt;
	
	@JsonProperty("device_id")
	private String deviceId;
	
	@JsonProperty("receipt")
	private Object receipt;
	
	@JsonProperty("error_code")
	private String errorCode;
	
	@JsonProperty("source_name")
	private String sourceName;
	
	@JsonProperty("currency_exchange_adjustment")
	private String currencyExchangeAdjustment;
	
	@JsonProperty("amount")
	private String amount;
	
	@JsonProperty("currency")
	private String currency;
	
	@JsonProperty("admin_graphql_api_id")
	private String adminGraphqlApiId;
	
	
	public ShopifyCreateTransaction createNewTransaction() {
		return new ShopifyCreateTransaction(currency, amount, kind, id);
	}


	public String getStatus() {
		return status;
	}

	public String getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}	
	
	
}
