package org.bytepoet.shopifysolo.manager.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.bytepoet.shopifysolo.solo.models.SoloProduct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Item {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonProperty
	private String name;
	@JsonProperty
	private String price;
	@JsonProperty
	private int quantity;
	@JsonProperty
	private String discount;
	@JsonProperty
	private String taxRate;
	
	protected Item() {}
	
	Item(SoloProduct soloProduct) {
		this.name = soloProduct.getName();
		this.price = soloProduct.getPrice();
		this.quantity = soloProduct.getQuantity();
		this.discount = soloProduct.getDiscount();
		this.taxRate = soloProduct.getTaxRate();
	}
	
	Item(ShopifyLineItem lineItem, String taxRate) {
		this.name = lineItem.getFullTitle();
		this.price = lineItem.getPricePerItem();
		this.quantity = lineItem.getQuantity();
		this.discount = lineItem.getDiscountPercent();
		this.taxRate = taxRate;
	}

	public String getName() {
		return name;
	}

	public String getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getDiscount() {
		return discount;
	}

	public String getTaxRate() {
		return taxRate;
	}
	
	
}
