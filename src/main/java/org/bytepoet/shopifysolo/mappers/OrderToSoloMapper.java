package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.solo.models.SoloProduct;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public abstract class OrderToSoloMapper<T extends SoloBillingObject, B extends SoloBillingObject.Builder<T>> {
	
	@Value("${soloapi.service-type}")
	private String serviceType;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.shipping-title}")
	private String shippingTitle;
	
	@Autowired
	private PaymentTypeMapper paymentTypeMapper;
	
	public T map(PaymentOrder order) {
		B builder = getBuilder();
		baseMappings(order, builder);
		additionalMappings(order, builder);
		return builder.build();
		
	}
	
	protected abstract B getBuilder();
	
	protected abstract void additionalMappings(PaymentOrder order, B builder);
	
	private void baseMappings(PaymentOrder order, B builder) {
		builder.serviceType(serviceType);
		builder.paymentType(paymentTypeMapper.getPaymentType(order));
		builder.email(order.getEmail());
		builder.isTaxed(false);
		builder.note(this.note);
		
		if (order.getItems() != null) {
			for (Item item : order.getItems()) {
				builder.addProduct(map(item));
			}
		}
	}
	
	private SoloProduct map(Item item) {
		return new SoloProduct.Builder()
				.name(item.getName())
				.quantity(item.getQuantity())
				.price(item.getPrice())
				.discount(item.getDiscount())
				.taxRate(item.getTaxRate())
				.build();
	}
}
