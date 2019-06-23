package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
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
	
	
	@Override
	protected SoloInvoice.Builder getBuilder() {
		return new SoloInvoice.Builder();
	}

	@Override
	protected void additionalMappings(ShopifyOrder order,
			org.bytepoet.shopifysolo.solo.models.SoloInvoice.Builder builder) {
		builder.receiptType(receiptType);
		builder.isFiscal(fiscalization);
		String note = this.note;
		if (!fiscalization) {
			note += "\n" + nonFiscalNote;
		}
		builder.note(note);
		
	}


	
	
	
}
