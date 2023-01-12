package org.bytepoet.shopifysolo.manager.models;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class ProductWebshopInfo {

	@JsonProperty
	protected String id;
	
	
	@JsonProperty
	private Integer quantity;
	
	
	@JsonProperty
	@Nonnull
	private String status;
	
	
	@Transient
	protected boolean isSynced = true;


	public Integer getQuantity() {
		return quantity;
	}


	protected void setQuantity(Integer quantity) {
		if (quantity != this.quantity) {
			isSynced = false;
		}
		this.quantity = quantity;
	}


	protected String getStatus() {
		return status;
	}


	protected void setStatus(String status) {
		if (!status.equals(this.status)) {
			isSynced = false;
		}
		this.status = status;
	}



	
	
	
	
}
