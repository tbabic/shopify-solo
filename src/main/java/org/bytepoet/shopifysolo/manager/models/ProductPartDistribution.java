package org.bytepoet.shopifysolo.manager.models;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Entity
public class ProductPartDistribution {

	@Id
	@JsonProperty
	@JsonSetter(nulls = Nulls.SKIP)
	private UUID id = UUID.randomUUID();
	
	@JsonIgnore
	@ManyToOne(optional = false, fetch=FetchType.LAZY)
    @JoinColumn(name = "productId")
	private Product product;
	
	@JsonProperty
	@ManyToOne(cascade = CascadeType.ALL, optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name = "productPartId")
	private ProductPart productPart;
	
	@JsonProperty
	private int partsUsed;
	
	@JsonProperty
	private int assignedQuantity;
	
	@JsonProperty
	public String productName() {
		return product.getName();
	}

	public UUID getId() {
		return id;
	}

	int getPartsUsed() {
		return partsUsed;
	}

	@JsonProperty
	public UUID productId() {
		return product.getId();
	}

	int getAssignedQuantity() {
		return assignedQuantity;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
	public int getAssignedToProducts() {
		return assignedQuantity / partsUsed;
	}
	
	public int getFreeForProducts() {
		return productPart.getSpareQuantity() / partsUsed;
	}
		
	
	public void update(int assignedQuantity, int partsUsed) {
		this.assignedQuantity = assignedQuantity;
		this.partsUsed = partsUsed;
	}
	
	public void update(int assignedQuantity, int partsUsed, ProductPart part) {
		this.assignedQuantity = assignedQuantity;
		this.partsUsed = partsUsed;
		this.productPart = part;
	}
}
