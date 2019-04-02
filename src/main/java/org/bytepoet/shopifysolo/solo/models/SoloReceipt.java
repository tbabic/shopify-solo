package org.bytepoet.shopifysolo.solo.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoloReceipt {
	
	private final String serviceType;
	
	private final String receiptType;
	
	private final String email;
	
	private final String paymentType;
	
	private final boolean isTaxed;
	
	private final List<SoloProduct> products;
	
	private SoloReceipt(Builder builder) {
		this.paymentType = builder.paymentType;
		this.serviceType = builder.serviceType;
		this.receiptType = builder.receiptType;
		this.email = builder.email;
		this.isTaxed = builder.isTaxed;
		this.products = Collections.unmodifiableList(builder.products);
		
	}
	
	public String getServiceType() {
		return serviceType;
	}
	
	public String getReceiptType() {
		return receiptType;
	}
	
	public String getPaymentType() {
		return paymentType;
	}

	public String getEmail() {
		return email;
	}
	
	

	public boolean isTaxed() {
		return isTaxed;
	}

	public List<SoloProduct> getProducts() {
		return products;
	}

	public static class Builder {
		private String serviceType;
		private String receiptType;
		private String paymentType;
		private String email;
		private boolean isTaxed;
		private List<SoloProduct> products = new ArrayList<>();

		public Builder serviceType(String serviceType) {
			this.serviceType = serviceType;
			return this;
		}
		public Builder receiptType(String receiptType) {
			this.receiptType = receiptType;
			return this;
		}
		public Builder paymentType(String paymentType) {
			this.paymentType = paymentType;
			return this;
		}
		public Builder email(String email) {
			this.email = email;
			return this;
		}
		
		public Builder isTaxed(boolean isTaxed) {
			this.isTaxed = isTaxed;
			return this;
		}
		
		public Builder addProduct(SoloProduct product) {
			products.add(product);
			return this;
		}
		
		public Builder addProduct(SoloProduct.Builder productBuilder) {
			products.add(productBuilder.build());
			return this;
		}
		
		public SoloReceipt build() {
			return new SoloReceipt(this);
		}
		
	}
	
	
	
	
	
	
}
