package org.bytepoet.shopifysolo.services;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Invoice;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.PaymentType;
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
public class InvoiceService {
	
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.non-fiscal-note}")
	private String nonFiscalNote;
	
	@Value("${webinvoice.item-id}")
	private String defaultItemId;
	
	
	@Autowired
	private WebInvoiceClient webInvoiceClient;
	

	
	
	
	public Invoice createInvoice(PaymentOrder order) {
		String note = remark(order);
		WebInvoice invoiceRequest = new WebInvoice.Builder()
				.paymentType(paymentType(order))
				.remark(note)
				.customer(new WebInvoiceCustomer.Builder()
						.name(order.getEmail())
						.email(order.getEmail())
						.build())
				.items(order.getItems().stream().map(item -> mapItem(item)).collect(Collectors.toList()))
				.build();
		
		String token = webInvoiceClient.getToken();
		WebInvoiceResponse response = CachedFunctionalService.<PaymentOrder,WebInvoiceResponse>cacheAndExecute(
				order, 
				o -> "invoice/"+o.getId().toString(), 
				o -> {
					return webInvoiceClient.createInvoice(token, invoiceRequest);
				});
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
	
	
	private String paymentType(PaymentOrder order) {
		if (order.getPaymentType() == PaymentType.BANK_TRANSACTION) {
			return "Account";
		}
		if (order.getPaymentType() == PaymentType.CREDIT_CARD) {
			return "Card";
		}
		throw new RuntimeException("unrecognized payment type");
	}
	
	private String remark(PaymentOrder order) {
		String note = this.note;
		if (order.getPaymentType() == PaymentType.BANK_TRANSACTION) {
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
				.price(df.format(item.getPriceWithTaxRate()))
				.discount(item.getDiscount())
				.quantity(Integer.toString(item.getQuantity()))
				.vat(item.getTaxRate())
				.build();
	}
}
