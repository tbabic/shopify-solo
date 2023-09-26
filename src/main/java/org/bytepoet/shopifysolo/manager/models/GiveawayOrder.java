package org.bytepoet.shopifysolo.manager.models;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;

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
	
	public GiveawayOrder(ShopifyOrder shopifyOrder, String platform) {
		if (shopifyOrder == null) {
			throw new RuntimeException("Shopify order can not be null");
		}
		this.creationDate = new Date();
		
		this.shopifyOrderId = shopifyOrder.getId();
		this.shopifyOrderNumber = shopifyOrder.getNumber();
		this.shippingInfo = new Address(shopifyOrder.getShippingAddress());
		this.contact = shopifyOrder.getEmail();
		this.creationDate = shopifyOrder.getCreated();
		this.items = shopifyOrder.getLineItems().stream().map(lineItem -> new Item(lineItem, "0")).collect(Collectors.toList());
		this.note = shopifyOrder.getNote();
		this.giveawayPlatform = platform;
		this.shippingType = ShippingType.HP_REGISTERED_MAIL;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.creationDate);
		calendar.add(Calendar.DATE, WAITING_LIST_PERIOD);
		this.sendingDate = calendar.getTime();
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
