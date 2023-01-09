package org.bytepoet.shopifysolo.manager.models;

import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Entity
public class Product {

	@Id
	@JsonProperty
    @JsonSetter(nulls = Nulls.SKIP)
	private UUID id = UUID.randomUUID();
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	@Embedded
	private ProductWebshopInfo webshopInfo;
	
	@JsonProperty
	@OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ProductPartDistribution> partDistributions;
	
	
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
	
	public ProductWebshopInfo getWebshopInfo() {
		if (webshopInfo == null) {
			webshopInfo = new ProductWebshopInfo();
		}
		return webshopInfo;
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
	
	@JsonIgnore
	@Transient
	public void setWebshopStatus(String status) {
		webshopInfo.status = status;
	}

	UUID getId() {
		return id;
	}
	
	void setPartDistributions(Set<ProductPartDistribution> partDistributions) {
		this.partDistributions = partDistributions;
		if (this.partDistributions != null) {
			this.partDistributions.stream().forEach(pd -> pd.setProduct(this));
		}
	}
	
	@JsonProperty
	@Transient
	public int getMaxFreeAssignments() {
		if (this.partDistributions == null) {
			return 0;
		}
		return this.partDistributions.stream().mapToInt(d -> d.getFreeForProducts()).min().orElse(0);
	}
	
	public static Product createFromWebshop(ShopifyProductVariant variant, ShopifyProduct shopifyProduct) {
		Product product = new Product();
		product.id = null;
		product.webshopInfo = new ProductWebshopInfo();
		product.webshopInfo.id = variant.id;
		product.webshopInfo.quantity = variant.quantity.intValue();
		product.webshopInfo.status = shopifyProduct.status;
		product.name = variant.title;
		return product;
	}
	
	

	
	
	
	
	
	
	
}
