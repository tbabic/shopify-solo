package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShopifyToSoloInvoiceMapper extends ShopifyToSoloMapper<SoloInvoice, SoloInvoice.Builder>{

	@Value("${soloapi.receipt-type}")
	private String receiptType;
	
	@Value("${soloapi.fiscalization}")
	private boolean fiscalization;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.non-fiscal-note}")
	private String nonFiscalNote;
	
	@Autowired
	private PaymentTypeMapper paymentTypeMapper;	
	
	@Override
	protected SoloInvoice.Builder getBuilder() {
		return new SoloInvoice.Builder();
	}

	@Override
	protected void additionalMappings(ShopifyOrder order,
			org.bytepoet.shopifysolo.solo.models.SoloInvoice.Builder builder) {
		builder.receiptType(receiptType);
		boolean isFiscal = isFiscal(order);
		builder.isFiscal(isFiscal);
		String note = this.note;
		if (!isFiscal) {
			note = nonFiscalNote + "\n" + note;
		}
		builder.note(note);
		
	}
	
	private boolean isFiscal(ShopifyOrder order) {
		return fiscalization && paymentTypeMapper.getPaymentType(order).isFiscal();
	}


	
	
	
}
