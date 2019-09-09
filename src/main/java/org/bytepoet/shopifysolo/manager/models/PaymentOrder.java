package org.bytepoet.shopifysolo.manager.models;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.bytepoet.shopifysolo.solo.models.SoloTender;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentOrder extends Order {
	
	private static final int WAITING_LIST_PERIOD = 7;
	
	@JsonProperty
	private String tenderId;
	@JsonProperty
	private String invoiceId;
	@JsonProperty
	private String shopifyOrderId;
	
	@JsonProperty
	private String tenderNumber;
	@JsonProperty
	private String invoiceNumber;
	@JsonProperty
	private String shopifyOrderNumber;
	
	
	@JsonProperty("contact")
	private String email;
	
	@JsonProperty
	private PaymentType paymentType;
	
	//TODO: extract to base class
	//TODO: dates

	@JsonProperty
	private Date paymentDate;
	
	@JsonProperty
	private Date sendingDate;
	
	//TODO: currency
	@JsonProperty
	private String currency = "HRK";
	// TODO: note
	@JsonProperty
	private String note;
	
	@JsonProperty
	private boolean isPaid;
	
	
	@JsonProperty
	private boolean isReceiptSent;
	
	@JsonProperty
	private boolean isFulfilled;
	
	protected PaymentOrder() {
		super();
	};
	
	public PaymentOrder(ShopifyOrder shopifyOrder, SoloTender soloTender, SoloInvoice soloInvoice, String shippingAddress) {
		if (soloTender == null && soloInvoice == null) {
			throw new RuntimeException("both tender and invoice can not be null");
		}
		this.creationDate = new Date();
		
		if (shopifyOrder != null) {
			this.shopifyOrderId = shopifyOrder.getId();
			this.shopifyOrderNumber = shopifyOrder.getNumber();
			this.shippingAddress = shopifyOrder.getFullAddress();
			this.email = shopifyOrder.getEmail();
			this.creationDate = shopifyOrder.getCreated();
		}
		if (shippingAddress != null) {
			this.shippingAddress = shippingAddress;
		}
		if (this.shippingAddress == null) {
			throw new RuntimeException("shipping address must not be empty, provide it directly or through shopify order");
		}
		
		if (soloTender != null) {
			updateFromSoloTender(soloTender);
		}
		
		if (soloInvoice != null) {
			updateFromSoloInvoice(soloInvoice);
		}
		
	}

	public void updateFromSoloTender(SoloTender soloTender) {
		this.tenderId = soloTender.getId();
		this.tenderNumber = soloTender.getNumber();
		updateFromSoloBillingObject(soloTender);
	}

	public void updateFromSoloInvoice(SoloInvoice soloInvoice) {
		this.invoiceId = soloInvoice.getId();
		this.invoiceNumber = soloInvoice.getNumber();
		this.paymentDate = new Date();
		this.isPaid = true;
		if (this.sendingDate != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, WAITING_LIST_PERIOD);
			this.sendingDate = calendar.getTime();
		}
		updateFromSoloBillingObject(soloInvoice);
	}
	
	private void updateFromSoloBillingObject(SoloBillingObject soloBillingObject) {
		this.email = soloBillingObject.getEmail();
		this.paymentType = PaymentType.fromSoloPaymentType(soloBillingObject.getPaymentType());
		this.items = soloBillingObject.getProducts().stream().map(product -> new Item(product)).collect(Collectors.toList());
	}
	
	public void setReceiptSent(boolean isReceiptSent) {
		this.isReceiptSent = isReceiptSent;
	}

	
}
