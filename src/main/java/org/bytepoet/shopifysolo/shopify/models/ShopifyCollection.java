package org.bytepoet.shopifysolo.shopify.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopifyCollection {

	public String id;
	
	@JsonProperty("sort_order")
	public String sortOrder;
	
	@JsonProperty
	public String title;
	
	@JsonProperty
	public List<ShopifyCollect> collects;
	
	@JsonProperty
	public Type type;
	
	public enum Type {
		SMART,
		CUSTOM
	}
}
