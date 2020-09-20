package org.bytepoet.shopifysolo.manager.models;

import java.util.Date;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Parent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class RefundInvoice {
	
	@Parent
	@JsonIgnore
	private Refund refund;

	@JsonProperty
	private String number;

	@JsonProperty
	private String id;

	@JsonProperty
	private String zki;

	@JsonProperty
	private String jir;

	@JsonProperty
	private String note;

	@JsonProperty
	private Date date;
	
	@JsonProperty
	private boolean isSent;

	public static class Builder {
		private Refund refund;
		private String number;
		private String id;
		private String zki;
		private String jir;
		private String note;
		private Date date;

		public Builder number(String number) {
			this.number = number;
			return this;
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder zki(String zki) {
			this.zki = zki;
			return this;
		}

		public Builder jir(String jir) {
			this.jir = jir;
			return this;
		}

		public Builder note(String note) {
			this.note = note;
			return this;
		}

		public Builder date(Date date) {
			this.date = date;
			return this;
		}

		public RefundInvoice build() {
			return new RefundInvoice(this);
		}
	}

	private RefundInvoice(Builder builder) {
		this.refund = builder.refund;
		this.number = builder.number;
		this.id = builder.id;
		this.zki = builder.zki;
		this.jir = builder.jir;
		this.note = builder.note;
		this.date = builder.date;
	}
	
	private RefundInvoice() {
		super();
	}
	
	@JsonIgnore
	public Refund getRefund() {
		return refund;
	}

	@JsonIgnore
	public void setRefund(Refund refund) {
		this.refund = refund;
	}

	public String getNumber() {
		return number;
	}

	public String getId() {
		return id;
	}

	public String getZki() {
		return zki;
	}

	public String getJir() {
		return jir;
	}

	public String getNote() {
		return note;
	}

	public Date getDate() {
		return date;
	}

	public boolean isSent() {
		return isSent;
	}

	void setSent(boolean isSent) {
		this.isSent = isSent;
	}

	void setNumber(String number) {
		this.number = number;
	}

	void setId(String id) {
		this.id = id;
	}

	void setDate(Date date) {
		this.date = date;
	}
	
	
	
	
	
	
	
}
