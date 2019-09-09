package org.bytepoet.shopifysolo.manager.models;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public abstract class Order {
	
	
	@JsonProperty
	private Long id;
	
	
	@JsonProperty
	protected String shippingAddress;
	
	@JsonProperty
	protected List<Item> items;
	
	
	//TODO: dates
	@JsonProperty
	protected Date creationDate;
	
	@JsonProperty
	protected Date sendingDate;

	public static enum Type {
		
		GIVEAWAY,
		PAYMENT
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
	
	
}
