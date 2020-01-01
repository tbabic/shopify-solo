package org.bytepoet.shopifysolo.manager.models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@DiscriminatorValue(OrderType.GIVEAWAY_ORDER)
public class GiveawayOrder extends Order {
	
	
	@JsonProperty
	private String giveawayPlatform;

	public GiveawayOrder() {
		
	}

	@Override
	public String getShippingSnapshot() {
		return giveawayPlatform + ": " + contact;
	}
	
	
	

}
