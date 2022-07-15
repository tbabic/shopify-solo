package org.bytepoet.shopifysolo.manager.models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ProductPartDistribution {

	@Id
	@JsonProperty
	private UUID id = UUID.randomUUID();
	
	@JsonIgnore
	@ManyToOne
    @JoinColumn(name = "productId")
	private Product product;
	
	@JsonProperty
	@ManyToOne
    @JoinColumn(name = "productPartId")
	private ProductPart productPart;
	
	@JsonProperty
	private int assignedQuantity;
	
	@JsonProperty
	public String productName() {
		return product.getName();
	}
	
	@JsonProperty
	public UUID productId() {
		return product.getId();
	}

	int getAssignedQuantity() {
		return assignedQuantity;
	}
	
	
	
	
}
