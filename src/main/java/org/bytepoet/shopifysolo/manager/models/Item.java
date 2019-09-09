package org.bytepoet.shopifysolo.manager.models;

import org.bytepoet.shopifysolo.solo.models.SoloProduct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
	
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
}
