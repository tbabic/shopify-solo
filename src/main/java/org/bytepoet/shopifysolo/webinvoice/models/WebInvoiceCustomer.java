package org.bytepoet.shopifysolo.webinvoice.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebInvoiceCustomer {

	@JsonProperty
	private String name;
	@JsonProperty
	private String oib;
	@JsonProperty
	private String address;
	@JsonProperty
	private String email;

	public static class Builder {
		private String name;
		private String oib;
		private String address;
		private String email;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder oib(String oib) {
			this.oib = oib;
			return this;
		}

		public Builder address(String address) {
			this.address = address;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public WebInvoiceCustomer build() {
			return new WebInvoiceCustomer(this);
		}
	}

	private WebInvoiceCustomer(Builder builder) {
		this.name = builder.name;
		this.oib = builder.oib;
		this.address = builder.address;
		this.email = builder.email;
	}
}
