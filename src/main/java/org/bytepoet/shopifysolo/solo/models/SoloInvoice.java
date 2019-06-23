package org.bytepoet.shopifysolo.solo.models;

public class SoloInvoice extends AbstractSoloInvoice{
	
	private final boolean isFiscal;
	
	private SoloInvoice(Builder builder) {
		super(builder);
		this.isFiscal = builder.isFiscal;
	}
	
	public boolean isFiscal() {
		return isFiscal;
	}


	public static class Builder extends AbstractSoloInvoice.Builder<SoloInvoice>{
		
		private boolean isFiscal;

		public Builder isFiscal(boolean isFiscal) {
			this.isFiscal = isFiscal;
			return this;
		}

		@Override
		public SoloInvoice build() {
			return new SoloInvoice(this);
		}
		
		
	}
	
}
