package org.bytepoet.shopifysolo.mappers;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloProduct;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public abstract class ShopifyToSoloMapper<T extends SoloBillingObject, B extends SoloBillingObject.Builder<T>> {
	
	@Value("${soloapi.service-type}")
	private String serviceType;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.shipping-title}")
	private String shippingTitle;
	
	@Value("${soloapi.webshop_note_format}")
	private String webshopNoteFormat;
	
	@Autowired
	private PaymentTypeMapper paymentTypeMapper;
	
	public T map(ShopifyOrder order) {
		B builder = getBuilder();
		baseMappings(order, builder);
		additionalMappings(order, builder);
		return builder.build();
		
	}
	
	protected abstract B getBuilder();
	
	protected abstract void additionalMappings(ShopifyOrder order, B builder);
	
	private void baseMappings(ShopifyOrder order, B builder) {
		builder.serviceType(serviceType);
		builder.paymentType(paymentTypeMapper.getPaymentType(order));
		builder.email(order.getEmail());
		builder.isTaxed(false);
		builder.note(getNote(order));
		
		if (order.getLineItems() != null) {
			for (ShopifyLineItem lineItem : order.getLineItems()) {
				builder.addProduct(map(lineItem, "0"));
			}
		}
		if (!order.getShippingPrice().equals("0.00")) {
			builder.addProduct( new SoloProduct.Builder()
					.name(shippingTitle)
					.quantity(1)
					.price(order.getShippingPrice())
					.taxRate("0")
					.discount("0")
					.build());
		}


	}
	
	
	private SoloProduct map(ShopifyLineItem lineItem, String taxRate) {
		return new SoloProduct.Builder()
				.name(productName(lineItem))
				.quantity(lineItem.getQuantity())
				.price(lineItem.getPricePerItem())
				.discount(lineItem.getDiscountPercent())
				.taxRate(taxRate)
				.build();
	}
	
	private String productName(ShopifyLineItem lineItem) {
		if (StringUtils.isBlank(lineItem.getVariantTitle())) {
			return lineItem.getTitle();
		}
		return lineItem.getTitle() + "/ " + lineItem.getVariantTitle();
	}
	
	private String getNote(ShopifyOrder order) {
		return MessageFormat.format(webshopNoteFormat, order.getNumber());
	}
	
}
