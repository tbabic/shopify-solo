package org.bytepoet.shopifysolo.webinvoice.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebInvoice {

	@JsonProperty
	private String paymentType;

	@JsonProperty
	private String note;

	@JsonProperty
	private WebInvoiceCustomer customer;

	@JsonProperty
	private List<WebInvoiceItem> items;
	
	private WebInvoice(Builder builder) {
		this.paymentType = builder.paymentType;
		this.note = builder.note;
		this.customer = builder.customer;
		this.items = builder.items;
	}

	public static class Builder {
		private String paymentType;
		private String note;
		private WebInvoiceCustomer customer;
		private List<WebInvoiceItem> items;

		public Builder paymentType(String paymentType) {
			this.paymentType = paymentType;
			return this;
		}

		public Builder remark(String note) {
			this.note = note;
			return this;
		}

		public Builder customer(WebInvoiceCustomer customer) {
			this.customer = customer;
			return this;
		}

		public Builder items(List<WebInvoiceItem> items) {
			this.items = items;
			return this;
		}
		
		public Builder addItem(WebInvoiceItem item) {
			if (this.items == null) {
				this.items = new ArrayList<WebInvoiceItem>();
			}
			this.items.add(item);
			return this;
		}

		public WebInvoice build() {
			return new WebInvoice(this);
		}
	}
}
