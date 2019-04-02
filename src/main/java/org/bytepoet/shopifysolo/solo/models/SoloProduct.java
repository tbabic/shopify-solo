package org.bytepoet.shopifysolo.solo.models;

public class SoloProduct {

	private final String unit = "2"; // kom
	
	private String name;
	private String price;
	private int quantity;
	private String discount;
	private String taxRate;
	
	private SoloProduct(Builder builder) {
		this.name = builder.name;
		this.price = builder.price;
		this.quantity = builder.quantity;
		this.discount = builder.discount;
		this.taxRate = builder.taxRate;
	}

	public String getUnit() {
		return unit;
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
	
	public static class Builder {
		private String name;
		private String price;
		private int quantity;
		private String discount;
		private String taxRate;
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder price(String price) {
			this.price = price;
			return this;
		}
		public Builder quantity(int quantity) {
			this.quantity = quantity;
			return this;
		}
		public Builder discount(String discount) {
			this.discount = discount;
			return this;
		}
		public Builder taxRate(String taxRate) {
			this.taxRate = taxRate;
			return this;
		}
		
		public SoloProduct build() {
			return new SoloProduct(this);
		}
		
		
		
	}
	
	
	
	
}
