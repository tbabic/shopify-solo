package org.bytepoet.shopifysolo.manager.models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@Entity
@DiscriminatorValue(OrderType.GIVEAWAY_ORDER)
@JsonTypeName(OrderType.GIVEAWAY_ORDER)
public class GiveawayOrder extends Order {
	
	
	@JsonProperty
	private String giveawayPlatform;

	public GiveawayOrder() {
		
	}

	@Override
	public String getShippingSnapshot() {
		return giveawayPlatform + ": " + contact;
	}

	@Override
	@Transient
	public OrderType getType() {
		return OrderType.GIVEAWAY;
	}
	
	
	

}
