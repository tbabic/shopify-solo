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
	
	public static PaymentType fromShopifyGateway (String paymentGateway) {
		if ("corvuspay".equals(paymentGateway)) {
			return PaymentType.CREDIT_CARD;
		}
		if ("Uplata na racun".equals(paymentGateway)) {
			return PaymentType.BANK_TRANSACTION;
		}
		throw new IllegalArgumentException("Unsupported payment type: " + paymentGateway);
	}
}
