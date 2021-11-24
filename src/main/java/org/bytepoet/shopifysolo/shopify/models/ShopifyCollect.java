package org.bytepoet.shopifysolo.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopifyCollect {

	@JsonProperty
	public String id;
	
	@JsonProperty("product_id")
	public String productId;
	
	@JsonProperty("collection_id")
	public String collectionId;
	
	@JsonProperty
	public String position;
}
