package org.bytepoet.shopifysolo.manager.models;

import java.util.Date;
import java.util.List;

import org.bytepoet.shopifysolo.manager.database.DatabaseTable.IdAccessor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "type")
@JsonSubTypes({ 
  @Type(value = GiveawayOrder.class, name = "GIVEAWAY"), 
  @Type(value = PaymentOrder.class, name = "PAYMENT") 
})
public abstract class Order extends IdAccessor {
	
	
	@JsonProperty(access = Access.READ_ONLY)
	private Long id;
	
	@JsonProperty
	protected String shippingAddress;
	
	@JsonProperty
	protected List<Item> items;	
	
	//TODO: dates
	@JsonProperty(access = Access.READ_ONLY)
	protected Date creationDate;
	
	@JsonProperty
	protected Date sendingDate;
	
	@JsonProperty(access = Access.READ_ONLY)
	private boolean isFulfilled;
	
	@JsonProperty(access = Access.READ_ONLY)
	private boolean isCanceled;

	public static enum Type {
		
		GIVEAWAY,
		PAYMENT
	}
	
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	public abstract void validate();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public abstract boolean matchShopifyOrder(String shopifyOrderId);

	public List<Item> getItems() {
		return items;
	}

	public boolean isFulfilled() {
		return isFulfilled;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getSendingDate() {
		return sendingDate;
	}

	public boolean isCanceled() {
		return isCanceled;
	}
	
	
	
	
	
	
	
	
	
}
