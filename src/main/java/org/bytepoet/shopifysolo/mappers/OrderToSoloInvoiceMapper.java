package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderToSoloInvoiceMapper extends OrderToSoloMapper<SoloInvoice, SoloInvoice.Builder>{

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
	protected void additionalMappings(PaymentOrder order,
			org.bytepoet.shopifysolo.solo.models.SoloInvoice.Builder builder) {
		builder.receiptType(receiptType);
		boolean isFiscal = isFiscal(order);
		builder.isFiscal(isFiscal);
		String note = this.note;
		if (!isFiscal) {
			note += "\n" + nonFiscalNote;
		}
		builder.note(note);
		
	}
	
	private boolean isFiscal(PaymentOrder order) {
		return fiscalization && order.getPaymentType().toSoloPaymentType().isFiscal();
	}


	
	
	
}
