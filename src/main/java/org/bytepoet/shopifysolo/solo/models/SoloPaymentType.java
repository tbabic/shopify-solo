package org.bytepoet.shopifysolo.solo.models;

public enum SoloPaymentType {

	BANK_DEPOSIT(1, false),
	CASH(2, true),
	CREDIT_CARD(3, true),
	CHECK(4, false),
	OTHERS(5, false);
	
	private final int value;
	private final boolean isFiscal;
	
	private SoloPaymentType(int value, boolean isFiscal) {
		this.value = value;
		this.isFiscal = isFiscal;
	}
	
	public int getValue() {
		return value;
	}
	public boolean isFiscal() {
		return isFiscal;
	}
	
	public static SoloPaymentType getFromValue(int value) {
		for (SoloPaymentType paymentType: SoloPaymentType.values()) {
			if (paymentType.value == value) {
				return paymentType;
			}
		}
		throw new IllegalArgumentException("Value: " + value + " is not one of the valid values");
	}
	
}
