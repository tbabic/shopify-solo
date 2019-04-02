package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloProduct;
import org.bytepoet.shopifysolo.solo.models.SoloReceipt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShopifyToSoloMapper {
	
	@Value("${soloapi.service-type}")
	private String serviceType;
	
	@Value("${soloapi.receipt-type}")
	private String receiptType;

	public SoloReceipt map(ShopifyOrder order) {
		SoloReceipt.Builder builder = new SoloReceipt.Builder();
		builder.serviceType(serviceType);
		builder.receiptType(receiptType);
		builder.email(order.getEmail());
		builder.isTaxed(order.isTaxesIncluded());
		if (order.getLineItems() != null) {
			for (ShopifyLineItem lineItem : order.getLineItems()) {
				builder.addProduct(map(lineItem, "0"));
			}
		}
		return builder.build();

	}
	
	
	private SoloProduct map(ShopifyLineItem lineItem, String taxRate) {
		return new SoloProduct.Builder()
				.name(lineItem.getTitle() + "/" + lineItem.getVariantTitle())
				.quantity(lineItem.getQuantity())
				.price(lineItem.getPricePerItem())
				.discount(lineItem.getDiscountPercent())
				.taxRate(taxRate)
				.build();
	}
	
}
