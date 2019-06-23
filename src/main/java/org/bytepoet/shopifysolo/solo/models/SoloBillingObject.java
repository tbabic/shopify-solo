package org.bytepoet.shopifysolo.solo.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SoloBillingObject {

	private final String serviceType;
	
	private final String email;
	
	private final String paymentType;
	
	private final boolean isTaxed;
	
	private final String note;
	
	private final List<SoloProduct> products;
	
	protected SoloBillingObject(Builder<? extends SoloBillingObject> builder) {
		this.paymentType = builder.paymentType;
		this.serviceType = builder.serviceType;
		this.email = builder.email;
		this.isTaxed = builder.isTaxed;
		this.products = Collections.unmodifiableList(builder.products);
		this.note = builder.note;
		
	}
	
	public String getServiceType() {
		return serviceType;
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
	
	public String getNote() {
		return note;
	}

	public abstract static class Builder<T extends SoloBillingObject> {
		
		private String serviceType;
		private String paymentType;
		private String email;
		private boolean isTaxed;
		private String note;
		private List<SoloProduct> products = new ArrayList<>();
		

		public Builder<T> serviceType(String serviceType) {
			this.serviceType = serviceType;
			return this;
		}
		
		public Builder<T> paymentType(String paymentType) {
			this.paymentType = paymentType;
			return this;
		}
		
		public Builder<T> email(String email) {
			this.email = email;
			return this;
		}
		
		public Builder<T> isTaxed(boolean isTaxed) {
			this.isTaxed = isTaxed;
			return this;
		}
		
		public Builder<T> addProduct(SoloProduct product) {
			products.add(product);
			return this;
		}
		
		public Builder<T> addProduct(SoloProduct.Builder productBuilder) {
			products.add(productBuilder.build());
			return this;
		}
		
		public Builder<T> note(String note) {
			this.note = note;
			return this;
		}
		
		public abstract T build();
		
	}
}