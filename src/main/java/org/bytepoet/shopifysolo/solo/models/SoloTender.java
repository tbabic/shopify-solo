package org.bytepoet.shopifysolo.solo.models;



public class SoloTender extends AbstractSoloInvoice{
	
	private SoloTender(Builder builder) {
		super(builder);
	}


	public static class Builder extends AbstractSoloInvoice.Builder<SoloTender> {

		@Override
		public SoloTender build() {
			return new SoloTender(this);
		}
		
	}
	
}
