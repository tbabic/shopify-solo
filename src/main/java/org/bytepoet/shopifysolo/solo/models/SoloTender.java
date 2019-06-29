package org.bytepoet.shopifysolo.solo.models;

public class SoloTender extends SoloBillingObject {

	private String number;

	private SoloTender(Builder builder) {
		super(builder);
		this.number = builder.number;
	}
	
	public String getNumber() {
		return number;
	}

	public static class Builder extends SoloBillingObject.Builder<SoloTender>{
		private String number;

		public Builder number(String number) {
			this.number = number;
			return this;
		}

		public SoloTender build() {
			return new SoloTender(this);
		}
	}
}
