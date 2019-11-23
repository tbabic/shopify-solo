package org.bytepoet.shopifysolo.solo.clients;

import org.apache.commons.lang3.RandomStringUtils;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.bytepoet.shopifysolo.solo.models.SoloTender;

public class MockSoloApiClient extends SoloApiClient {
	
	private static int number = 0;
	
	private static final String BASE_PDF_URL = "http://base-pdf.url/";
	
	
	public SoloInvoice createInvoice(SoloInvoice receipt) {
		SoloInvoice.Builder builder = new SoloInvoice.Builder()
				.isFiscal(receipt.isFiscal())
				.receiptType(receipt.getReceiptType());
		copyToBuilder(builder, receipt);
		addNewPropertiesToBuilder(builder);
		return builder.build();
		
	}
	

	public SoloTender createTender(SoloTender tender) {
		SoloTender.Builder builder = new SoloTender.Builder();
		copyToBuilder(builder, tender);
		addNewPropertiesToBuilder(builder);
		return builder.build();
	}
	
	private void copyToBuilder(SoloBillingObject.Builder<?> builder, SoloBillingObject solo) {
		builder.serviceType(solo.getServiceType())
			.paymentType(solo.getPaymentType())
			.email(solo.getEmail())
			.isTaxed(solo.isTaxed())
			.note(solo.getNote())
			.products(solo.getProducts());
	}
	
	private void addNewPropertiesToBuilder(SoloBillingObject.Builder<?> builder) {
		String id = RandomStringUtils.randomAlphanumeric(10);
		builder.id(id);
		builder.pdfUrl(BASE_PDF_URL + id);
		synchronized(this) {
			number++;
			builder.number(String.valueOf(number));
		}
		
	}
}
