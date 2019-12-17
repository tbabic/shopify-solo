package org.bytepoet.shopifysolo.mappers;

import java.util.List;

import org.bytepoet.shopifysolo.manager.models.PaymentType;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GatewayToPaymentTypeMapper {

	@Value("${solofy.default-payment-type}")
	private String defaultPaymentType;
	
	@Value("${shopify.bank-deposit-gateway}")
	private List<String> bankDepositGateway;
	
	@Value("${shopify.card-gateway}")
	private List<String> cardGateway;
	
	public PaymentType getPaymentType(ShopifyOrder order) {
		return getPaymentType(order.getGateway());
	}
	
	public PaymentType getPaymentType(String paymentGateway) {
		if(cardGateway.contains(paymentGateway)) {
			return PaymentType.CREDIT_CARD;
		}
		if(bankDepositGateway.contains(paymentGateway)) {
			return PaymentType.BANK_TRANSACTION;
		}
		return PaymentType.valueOf(defaultPaymentType);
	}
	
}
