package org.bytepoet.shopifysolo.manager.models;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import org.springframework.util.CollectionUtils;

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
		if (webshopInfo == null) {
			return 0;
		} else {
			return webshopInfo.getQuantity();
		}
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
		webshopInfo.setQuantity(quantity);
	}
	
	@JsonIgnore
	@Transient
	public void setWebshopStatus(String status) {
		webshopInfo.setStatus(status);
	}

	public UUID getId() {
		return id;
	}
	
	void setPartDistributions(Set<ProductPartDistribution> partDistributions) {
		this.partDistributions = partDistributions;
		if (this.partDistributions != null) {
			this.partDistributions.stream().forEach(pd -> pd.setProduct(this));
		}
	}
	
	public Set<ProductPartDistribution> getPartDistributions() {
		return partDistributions;
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
		product.webshopInfo.setQuantity(variant.quantity.intValue());
		product.webshopInfo.setStatus(shopifyProduct.status);
		product.name = variant.title;
		product.webshopInfo.isSynced = true;
		return product;
	}
	
	public void moveQuantity(int quantity) {
		if (this.webshopInfo == null) {
			throw new RuntimeException("Product not on webshop, quantity can not be moved");
		}
		
		if (quantity > 0) {
			for(ProductPartDistribution distribution : this.partDistributions) {
				if (!distribution.availableToMove(quantity)) {
					throw new RuntimeException("Not enough parts available to be moved");
				}
			}
		} else if (this.getQuantity() < Math.abs(quantity)) {
			throw new RuntimeException("Trying to move too many products from webshop");
		}
		
		this.webshopInfo.setQuantity(this.getQuantity() + quantity);

	}
	
	public boolean validateAvailableMaterials() {
		if (CollectionUtils.isEmpty(partDistributions) && this.getQuantity() > 0) {
			return false;
		}
		
		for(ProductPartDistribution distribution : this.partDistributions) {
			if (!distribution.checkAvailable()) {
				return false;
			}
		}
		return true;
	}
	
	public void reduceAvailability(int quantity) {
		this.partDistributions.forEach(d -> d.reduceAvailability(quantity));
	}
	
	public void sync() {
		this.webshopInfo.isSynced = true;
	}
	
	public boolean isSynced() {
		return this.webshopInfo.isSynced;
	}
	

	
	@JsonIgnore
	public List<ProductPart> getParts() {
		return this.partDistributions.stream().map(d -> d.getProductPart()).collect(Collectors.toList());
		
	}
	
	
	
}
