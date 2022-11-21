package org.bytepoet.shopifysolo.manager.models;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.common.base.Optional;

@Entity
public class Product {

	@Id
	@JsonProperty
	private UUID id = UUID.randomUUID();
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	@Embedded
	private ProductWebshopInfo webshopInfo;
	
	@JsonProperty
	@OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<ProductPartDistribution> partDistributions;
	
	
	@JsonProperty(access = Access.READ_ONLY)
	public int getQuantity() {
		if (partDistributions == null) {
			return 0;
		}
		return partDistributions.stream().mapToInt(part -> part.getAssignedQuantity())
				.min()
				.orElse(0);
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	@Transient
	public String getWebshopId() {
		return webshopInfo == null ? null : webshopInfo.id;
	}
	
	@JsonIgnore
	@Transient
	public void setWebshopQuantity(int quantity) {
		webshopInfo.quantity = quantity;
	}

	UUID getId() {
		return id;
	}
	
	public static Product createFromWebshop(ShopifyProductVariant variant) {
		Product product = new Product();
		product.id = null;
		product.webshopInfo = new ProductWebshopInfo();
		product.webshopInfo.id = variant.id;
		product.webshopInfo.quantity = variant.quantity.intValue();
		product.name = variant.title;
		return product;
	}
	
	
	
	
}
