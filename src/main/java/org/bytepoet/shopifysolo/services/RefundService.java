package org.bytepoet.shopifysolo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Currency;
import org.bytepoet.shopifysolo.manager.models.Invoice;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.PaymentType;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.manager.models.RefundInvoice;
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
	
	@Value("${soloapi.refund-note-year}")
	private String refundNoteYear;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.non-fiscal-note}")
	private String nonFiscalNote;
	
	@Value("${webinvoice.item-id}")
	private String defaultItemId;
	
	private Currency defaultCurrency = Currency.EUR;
	
	
	@Autowired
	private WebInvoiceClient webInvoiceClient;
	

	
	
	
	public RefundInvoice createInvoice(Refund refund) {
		String note = remark(refund);
		WebInvoice invoiceRequest = new WebInvoice.Builder()
				.paymentType(paymentType(refund))
				.remark(note)
				.customer(new WebInvoiceCustomer.Builder()
						.name(refund.getOrder().getEmail())
						.email(refund.getOrder().getEmail())
						.build())
				.items(refund.getItems().stream().map(item -> mapItem(item, refund.getOrder().getCurrency())).collect(Collectors.toList()))
				.build();
		
		String token = webInvoiceClient.getToken();
		WebInvoiceResponse response = webInvoiceClient.createInvoice(token, invoiceRequest);
		WebInvoiceDetails details = webInvoiceClient.getInvoiceDetails(token, response.invoiceId);
		
		RefundInvoice invoice = new RefundInvoice.Builder()
				.date(details.getDate())
				.id(response.invoiceId)
				.jir(details.getJir())
				.zki(details.getZki())
				.note(note)
				.number(details.getInvoiceNumber())
				.vatAmount(details.getVatAmount())
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
		Calendar invoiceCal = Calendar.getInstance();
		invoiceCal.setTime(refund.getOrder().getInvoice().getDate());
		Calendar currentCal = Calendar.getInstance();
		String note;
		if (invoiceCal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR)) {
			String year = Long.toString(invoiceCal.get(Calendar.YEAR)).replace(",", "").replace(".", "");
			note = MessageFormat.format(refundNoteYear, refund.getOrder().getInvoiceNumber(), year);
		} else {
			note = MessageFormat.format(refundNote, refund.getOrder().getInvoiceNumber());
		}
		
		
		if (refund.getOrder().getPaymentType() == PaymentType.BANK_TRANSACTION) {
			note += "\n" + this.nonFiscalNote;
		}
		return note;
	}
	
	private WebInvoiceItem mapItem(Item item,  Currency orderCurrency) {
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(newSymbols);
		String price;
		if (orderCurrency == defaultCurrency) {
			price = df.format(item.getPriceWithTaxRate().negate());
		} else {
			BigDecimal priceValue = orderCurrency.convertTo(defaultCurrency, item.getPriceWithTaxRate());
			price = df.format(priceValue.negate());
		}
		
		return new WebInvoiceItem.Builder()
				.itemId(defaultItemId)
				.name(item.getName())
				.price(price)
				.discount(item.getDiscount())
				.quantity(Integer.toString(item.getQuantity()))
				.vat(item.getTaxRate())
				.build();
	}
}
