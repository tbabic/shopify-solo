package org.bytepoet.shopifysolo.mappers;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
		if (StringUtils.isNotBlank(order.getTags())) {
			for (String cg : cardGateway) {
				if (order.getTags().toLowerCase().contains(cg.toLowerCase())) {
					return PaymentType.CREDIT_CARD;
				}
			}
			
			for (String cg : bankDepositGateway) {
				if (order.getTags().toLowerCase().contains(cg.toLowerCase())) {
					return PaymentType.BANK_TRANSACTION;
				}
			}
		}
		return getPaymentType(order.getGateways());
	}
	
	public PaymentType getPaymentType(List<String> paymentGateways) {
		if (paymentGateways == null || paymentGateways.isEmpty()) {
			return PaymentType.valueOf(defaultPaymentType);
		}
		
		if (paymentGateways.stream().anyMatch(g -> cardGateway.contains(g))) {
			return PaymentType.CREDIT_CARD;
		}
		
		if (paymentGateways.stream().anyMatch(g -> bankDepositGateway.contains(g))) {
			return PaymentType.BANK_TRANSACTION;
		}
		
		return PaymentType.valueOf(defaultPaymentType);
	}
	
}
