package org.bytepoet.shopifysolo.webinvoice.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebInvoiceItem {

	@JsonProperty
	private String itemId = "1";
	@JsonProperty
	private String quantity;
	@JsonProperty
	private String name;
	@JsonProperty
	private String price;
	@JsonProperty
	private String discount;
	@JsonProperty
	private String vat;

	public static class Builder {
		private String itemId;
		private String quantity;
		private String name;
		private String price;
		private String discount;
		private String vat;

		public Builder itemId(String itemId) {
			this.itemId = itemId;
			return this;
		}

		public Builder quantity(String quantity) {
			this.quantity = quantity;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder price(String price) {
			this.price = price;
			return this;
		}

		public Builder discount(String discount) {
			this.discount = discount;
			return this;
		}

		public Builder vat(String vat) {
			this.vat = vat;
			return this;
		}

		public WebInvoiceItem build() {
			return new WebInvoiceItem(this);
		}
	}

	private WebInvoiceItem(Builder builder) {
		this.itemId = builder.itemId;
		this.quantity = builder.quantity;
		this.name = builder.name;
		this.price = builder.price;
		this.discount = builder.discount;
		this.vat = builder.vat;
	}
}
