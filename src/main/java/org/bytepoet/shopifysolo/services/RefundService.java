package org.bytepoet.shopifysolo.services;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Invoice;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.PaymentType;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.webinvoice.client.WebInvoiceClient;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoice;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceCustomer;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceDetails;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceItem;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RefundService {
	
	@Value("${soloapi.refund-note}")
	private String refundNote;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.non-fiscal-note}")
	private String nonFiscalNote;
	
	@Value("${webinvoice.item-id}")
	private String defaultItemId;
	
	
	@Autowired
	private WebInvoiceClient webInvoiceClient;
	

	
	
	
	public Invoice createInvoice(Refund refund) {
		String note = remark(refund);
		WebInvoice invoiceRequest = new WebInvoice.Builder()
				.paymentType(paymentType(refund))
				.remark(note)
				.customer(new WebInvoiceCustomer.Builder()
						.name(refund.getOrder().getEmail())
						.email(refund.getOrder().getEmail())
						.build())
				.items(refund.getItems().stream().map(item -> mapItem(item)).collect(Collectors.toList()))
				.build();
		
		String token = webInvoiceClient.getToken();
		WebInvoiceResponse response = webInvoiceClient.createInvoice(token, invoiceRequest);
		WebInvoiceDetails details = webInvoiceClient.getInvoiceDetails(token, response.invoiceId);
		
		Invoice invoice = new Invoice.Builder()
				.date(details.getDate())
				.id(response.invoiceId)
				.jir(details.getJir())
				.zki(details.getZki())
				.note(note)
				.number(details.getInvoiceNumber())
				.build();
		
		return invoice;		
	}
	
	
	private String paymentType(Refund refund) {
		if (refund.getOrder().getPaymentType() == PaymentType.BANK_TRANSACTION) {
			return "Account";
		}
		if (refund.getOrder().getPaymentType() == PaymentType.CREDIT_CARD) {
			return "Card";
		}
		throw new RuntimeException("unrecognized payment type");
	}
	
	private String remark(Refund refund) {
		String note = MessageFormat.format(refundNote, refund.getOrder().getInvoiceNumber());
		
		if (refund.getOrder().getPaymentType() == PaymentType.BANK_TRANSACTION) {
			note += "\n" + this.nonFiscalNote;
		}
		return note;
	}
	
	private WebInvoiceItem mapItem(Item item) {
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(newSymbols);
		
		
		return new WebInvoiceItem.Builder()
				.itemId(defaultItemId)
				.name(item.getName())
				.price(df.format(-item.getPriceWithTaxRate()))
				.discount(item.getDiscount())
				.quantity(Integer.toString(item.getQuantity()))
				.vat(item.getTaxRate())
				.build();
	}
}
