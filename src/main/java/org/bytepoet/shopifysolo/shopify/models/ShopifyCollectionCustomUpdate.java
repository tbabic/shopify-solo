package org.bytepoet.shopifysolo.shopify.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopifyCollectionCustomUpdate {
	
	@JsonProperty("custom_collection")
	public ShopifyCustomCollection customCollection;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ShopifyCustomCollection {
		
		public String id;
		
		public List<ShopifyCollect> collects;
		
		@JsonProperty("sort_order")
		public String sortOrder;
	}
	
	
	public void addCollect(ShopifyCollect collect) {
		if (customCollection == null) {
			customCollection = new ShopifyCustomCollection();
		}
		if (customCollection.collects == null) {
			customCollection.collects = new ArrayList<>();
		}
		customCollection.collects.add(collect);
	}
	
	@JsonIgnore
	public void setSortOrder(String sortOrder) {
		if (customCollection == null) {
			customCollection = new ShopifyCustomCollection();
		}
		customCollection.sortOrder = sortOrder;
	}
	
	
}
