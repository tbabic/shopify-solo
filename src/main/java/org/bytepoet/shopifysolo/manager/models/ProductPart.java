package org.bytepoet.shopifysolo.manager.models;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Entity
public class ProductPart {

	@Id
	@JsonProperty
	@JsonSetter(nulls = Nulls.SKIP)
	private UUID id = UUID.randomUUID();
	
	@JsonIgnore
	@OneToMany(mappedBy="productPart", orphanRemoval = false, fetch=FetchType.LAZY)
	private Set<ProductPartDistribution> distributions;
	
	@JsonProperty
	private int quantity;
	
	@JsonProperty
	private String title;
	
	@JsonProperty
	private String description;
	
	@JsonProperty
	private String link;
	
	@JsonProperty
	private String alternativeLink;
	
	@JsonProperty
	private String alternativeDescription;
	
	@JsonProperty
	private String alternativeLink2;
	
	@JsonProperty
	private String alternativeDescription2;
	
	@Transient
	@JsonProperty
	private boolean optional = false;
	
	
	@Transient
	@JsonIgnore
	private Integer assignedQuantityTransient;

	public int getQuantity() {
		return quantity;
	}
	
	
	
	
	@JsonIgnore
	public Set<ProductPartDistribution> getDistributions() {
		return distributions;
	}





	@JsonProperty
	public int getAssignedQuantity() {
		if (assignedQuantityTransient != null) {
			return assignedQuantityTransient;
		}
		if (distributions == null) {
			return 0;
		}
		assignedQuantityTransient = distributions.stream().mapToInt(d -> d.getAssignedQuantity()).sum();
		return assignedQuantityTransient;
	}
	
	@JsonProperty
	public int getSpareQuantity() {
		return quantity - getAssignedQuantity();
	}
	
	@JsonProperty(access=Access.READ_ONLY)
	public List<ProductPartDistributionInternal> getPartDistributions() {
		return distributions.stream().map(d -> ProductPartDistributionInternal.from(d)).collect(Collectors.toList());
	}
	
	@JsonProperty(access=Access.READ_ONLY)
	public List<UUID> getAssignedProductIds() {
		return distributions.stream().map(d -> d.productId()).collect(Collectors.toList());
	}
	
	public boolean availableToMove(int individualPartsQuantity) {
		if (!this.optional && (this.getSpareQuantity() - individualPartsQuantity) < 0) {
			return false;
		}
		
		return true;
	}
	
	public boolean checkAvialable() {
		if (!this.optional && this.getSpareQuantity() < 0) {
			return false;
		}
		
		return true;
	}
	
	public void reduceQuantity(int individualPartsQuantity) {
		this.quantity -= individualPartsQuantity;
	}
	
	public void addQuantity(int individualPartsQuantity) {
		this.quantity += individualPartsQuantity;
	}
	
	private static class ProductPartDistributionInternal {
		
		public static ProductPartDistributionInternal from (ProductPartDistribution distribution) {
			ProductPartDistributionInternal distro = new ProductPartDistributionInternal();
			distro.id = distribution.getId();
			distro.productId = distribution.productId();
			distro.productName = distribution.productName();
			distro.partsUsed = distribution.getPartsUsed();
			distro.assignedQuantity = distribution.getAssignedQuantity();
			distro.assignedToProducts = distribution.getAssignedToProducts();
			distro.freeForProducts = distribution.getFreeForProducts();
			return distro;
		}

		@JsonProperty
		private UUID id = UUID.randomUUID();
		
		@JsonProperty
		private UUID productId;
		
		@JsonProperty
		private String productName;
				
		@JsonProperty
		private int partsUsed;
		
		@JsonProperty
		private int assignedQuantity;
		
		@JsonProperty
		private int assignedToProducts;
		
		@JsonProperty
		private int freeForProducts;
	}
}
