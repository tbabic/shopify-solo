package org.bytepoet.shopifysolo.solo.models;

public class SoloInvoice extends SoloBillingObject{
	
	private final String receiptType;
	private final boolean isFiscal;
	
	private SoloInvoice(Builder builder) {
		super(builder);
		this.receiptType = builder.receiptType;
		this.isFiscal = builder.isFiscal;
	}
	
	public String getReceiptType() {
		return receiptType;
	}
	
	public boolean isFiscal() {
		return isFiscal;
	}


	public static class Builder extends SoloBillingObject.Builder<SoloInvoice>{
		
		private String receiptType;
		private boolean isFiscal;
		
		public Builder receiptType(String receiptType) {
			this.receiptType = receiptType;
			return this;
		}

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
