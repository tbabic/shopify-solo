package org.bytepoet.shopifysolo.solo.models;

public class SoloTender extends SoloBillingObject {


	private SoloTender(Builder builder) {
		super(builder);
	}
	

	public static class Builder extends SoloBillingObject.Builder<SoloTender>{

		public SoloTender build() {
			return new SoloTender(this);
		}
	}
}
