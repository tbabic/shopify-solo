package org.bytepoet.shopifysolo.manager.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Item {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty
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
	
	@JsonProperty
	private double weight;
	
	@JsonProperty
	private String shopifyId;
	
	@JsonProperty
	private boolean isShipping;
	
	@ManyToOne
    @JoinColumn(name = "refundId")
	@JsonIgnore
	private Refund refund;
	
	protected Item() {}
	
	public Item(ShopifyLineItem lineItem, String taxRate) {
		this.name = lineItem.getFullTitle();
		this.price = lineItem.getPricePerItem();
		this.quantity = lineItem.getQuantity();
		this.discount = lineItem.getDiscountPercent();
		this.taxRate = taxRate;
		this.weight = lineItem.getGrams();
		this.shopifyId = lineItem.getVariantId();
		applyTaxRate();
	}
	
	

	public Item(String name, String price, int quantity, String discount, String taxRate) {
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
	
	public double getWeight() {
		return weight;
	}
	
	public String getShopifyId() {
		return shopifyId;
	}

	public Long getId() {
		return id;
	}
	
	public boolean updateFromShopify(ShopifyLineItem item) {
		if (this.weight == 0) {
			this.weight = item.getGrams();
		}
		this.shopifyId = item.getVariantId();
		if (item.getGrams() == 0) {
			return false;
		}
		return true;
	}
	
	public boolean updateFromShopify(ShopifyProductVariant variant) {
		if (this.weight == 0) {
			this.weight = Double.parseDouble(variant.grams);
		}
		this.shopifyId = variant.id;
		if (this.weight == 0) {
			return false;
		}
		return true;
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
	
	public double getTotalPrice() {
		double taxRate = 1;
		if (StringUtils.isNotBlank(this.taxRate)) {
			taxRate = 1 + (Double.parseDouble(this.taxRate) / 100);
		}
		
		double discount = 1;
		if (StringUtils.isNotBlank(this.discount)) {
			discount = 1 - (Double.parseDouble(this.discount) / 100);
		}
		double price = 0;
		if (StringUtils.isNotBlank(this.price)) {
			price = Double.parseDouble(this.price) * taxRate * discount;
		}
		
		BigDecimal bd = BigDecimal.valueOf(price);
	    bd = bd.setScale(2, RoundingMode.HALF_UP);
	    price = bd.doubleValue();
		
		return price*quantity;
		
		
	}
	
	public String totalPriceString() {
		DecimalFormat df = getDecimalFormat();
		return df.format(getTotalPrice());
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
	
	public double getPriceWithTaxRate() {
		double taxRate = 1;
		if (StringUtils.isNotBlank(this.taxRate)) {
			taxRate = 1 + (Double.parseDouble(this.taxRate) / 100);
		}
		double price = 0;
		if (StringUtils.isNotBlank(this.price)) {
			price = Double.parseDouble(this.price) * taxRate;
		}
		return price;
	}

	private DecimalFormat getDecimalFormat() {
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(newSymbols);
		return df;
	}
	
	@JsonProperty
	@Transient
	public boolean isRefunded() {
		return refund!= null;
	}
	
	@JsonProperty
	@Transient
	public Long getRefundId() {
		if (refund == null) {
			return null;
		}
		return refund.getId();
	}
	
	@JsonProperty
	@Transient
	public void setRefundId(Long refundId) {
		if (refundId == null) {
			refund = null;
			return;
		}
		if (refund == null) {
			refund = new Refund();
		}
		refund.setId(refundId);
	}

	public Refund getRefund() {
		return refund;
	}

	public void setRefund(Refund refund) {
		this.refund = refund;
	}
	
	@JsonIgnore
	public double getDiscountAmount() {
		double discountMultiplier = Double.parseDouble(this.discount) / 100;;
		return this.getPriceWithTaxRate() * quantity * discountMultiplier;
	}

	public boolean isShipping() {
		return isShipping;
	}

	public void setShipping(boolean isShipping) {
		this.isShipping = isShipping;
	}
	
	
	
	
	
}
