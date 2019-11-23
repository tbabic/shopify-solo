package org.bytepoet.shopifysolo.manager.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GiveawayOrder extends Order {
	
	@JsonProperty
	private String contact;
	//TODO: extract following properties to base class

	public GiveawayOrder() {
		
	}

	@Override
	public boolean matchShopifyOrder(String shopifyOrderId) {
		return false;
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
