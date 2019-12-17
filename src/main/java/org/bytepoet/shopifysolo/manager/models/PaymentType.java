package org.bytepoet.shopifysolo.manager.models;

import org.bytepoet.shopifysolo.solo.models.SoloPaymentType;

public enum PaymentType {

	CREDIT_CARD,
	BANK_TRANSACTION;
	
	public static PaymentType fromSoloPaymentType (SoloPaymentType soloPaymentType) {
		if (soloPaymentType == SoloPaymentType.CREDIT_CARD) {
			return PaymentType.CREDIT_CARD;
		}
		if (soloPaymentType == SoloPaymentType.BANK_DEPOSIT) {
			return PaymentType.BANK_TRANSACTION;
		}
		throw new IllegalArgumentException("Unsupported payment type: " + soloPaymentType);
	}
	
	public SoloPaymentType toSoloPaymentType() {
		if(this == PaymentType.CREDIT_CARD) {
			return SoloPaymentType.CREDIT_CARD;
		}
		if (this == PaymentType.BANK_TRANSACTION) {
			return SoloPaymentType.BANK_DEPOSIT;
		}
		throw new RuntimeException("Can not convert to soloPaymentType: " + this);
	}
}
