package org.bytepoet.shopifysolo.manager.models;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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
		applyTaxRate();
	}
	
	

	Item(String name, String price, int quantity, String discount, String taxRate) {
		this.name = name;
		this.price = price;
		this.quantity = quantity;
		this.discount = discount;
		this.taxRate = taxRate;
		applyTaxRate();
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
	
	public void applyTaxRate(String taxRate) {
		double oldTaxRate = Double.parseDouble(this.taxRate);
		double newTaxRate = Double.parseDouble(taxRate);
		if (oldTaxRate == newTaxRate) {
			return;
		}
		if (oldTaxRate != 0.0) {
			removeTaxRate();
			this.taxRate = "0";
		}
		this.taxRate = taxRate;
		applyTaxRate();
		
	}
	
	private void removeTaxRate() {
		double priceWithoutTax = Double.parseDouble(this.price);
		double taxRate = Double.parseDouble(this.taxRate) / 100;
		double totalPrice = priceWithoutTax * taxRate;
		DecimalFormat df = getDecimalFormat();
		this.price = df.format(totalPrice);
	}
	
	private void applyTaxRate() {
		double totalPrice = Double.parseDouble(this.price);
		double taxRate = Double.parseDouble(this.taxRate) / 100;
		double priceWithoutTax = totalPrice / (1.0+taxRate);
		DecimalFormat df = getDecimalFormat();
		this.price = df.format(priceWithoutTax);
	}

	private DecimalFormat getDecimalFormat() {
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(newSymbols);
		return df;
	}
	
}
