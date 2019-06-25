package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloPaymentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentTypeMapper {

	@Value("${soloapi.payment-type}")
	private int defaultPaymentType;
	
	@Value("${shopify.bank-deposit-gateway}")
	private String bankDepositGateway;
	
	@Value("${shopify.card-gateway}")
	private String cardGateway;
	
	public SoloPaymentType getPaymentType(ShopifyOrder order) {
		return getPaymentType(order.getGateway());
	}
	
	public SoloPaymentType getPaymentType(String paymentGateway) {
		if(cardGateway.equals(paymentGateway)) {
			return SoloPaymentType.CREDIT_CARD;
		}
		if(bankDepositGateway.equals(paymentGateway)) {
			return SoloPaymentType.BANK_DEPOSIT;
		}
		return SoloPaymentType.getFromValue(defaultPaymentType);
	}
	
}
