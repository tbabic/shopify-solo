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
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentOrder extends Order {
	
	private static final int WAITING_LIST_PERIOD = 7;
	
	@JsonProperty(access = Access.READ_ONLY)
	private String tenderId;
	@JsonProperty(access = Access.READ_ONLY)
	private String invoiceId;
	@JsonProperty(access = Access.READ_ONLY)
	private String shopifyOrderId;
	
	@JsonProperty(access = Access.READ_ONLY)
	private String tenderNumber;
	@JsonProperty(access = Access.READ_ONLY)
	private String invoiceNumber;
	@JsonProperty(access = Access.READ_ONLY)
	private String shopifyOrderNumber;
	
	
	@JsonProperty("contact")
	private String email;
	
	@JsonProperty
	private PaymentType paymentType;

	@JsonProperty
	private Date paymentDate;
	
	//TODO: currency
	@JsonProperty
	private String currency = "HRK";
	// TODO: note
	@JsonProperty
	private String note;
	
	@JsonProperty(access = Access.READ_ONLY)
	private boolean isPaid;
	
	
	@JsonProperty
	private boolean isReceiptSent;
	
	
	
	protected PaymentOrder() {
		super();
	};
	
	public PaymentOrder(ShopifyOrder shopifyOrder, SoloInvoice soloInvoice) {
		if (shopifyOrder == null || soloInvoice == null) {
			throw new RuntimeException("Shopify order and solo invoice can not be null");
		}
		this.creationDate = new Date();
		
		this.shopifyOrderId = shopifyOrder.getId();
		this.shopifyOrderNumber = shopifyOrder.getNumber();
		this.shippingAddress = shopifyOrder.getFullAddress();
		this.email = shopifyOrder.getEmail();
		this.creationDate = shopifyOrder.getCreated();

		updateFromSoloInvoice(soloInvoice, new Date());
	}
	
	public PaymentOrder(ShopifyOrder shopifyOrder, SoloTender soloTender) {
		if (shopifyOrder == null || soloTender == null) {
			throw new RuntimeException("Shopify order and solo tender can not be null");
		}
		this.creationDate = new Date();
		
		this.shopifyOrderId = shopifyOrder.getId();
		this.shopifyOrderNumber = shopifyOrder.getNumber();
		this.shippingAddress = shopifyOrder.getFullAddress();
		this.email = shopifyOrder.getEmail();
		this.creationDate = shopifyOrder.getCreated();

		updateFromSoloTender(soloTender);
	}

	public void updateFromSoloInvoice(SoloInvoice soloInvoice, Date paymentDate) {
		this.invoiceId = soloInvoice.getId();
		this.invoiceNumber = soloInvoice.getNumber();
		if (paymentDate == null) {
			paymentDate = new Date();
		}
		this.isPaid = true;
		this.paymentDate = paymentDate;
		if (this.sendingDate != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, WAITING_LIST_PERIOD);
			this.sendingDate = calendar.getTime();
		}
		updateFromSoloBillingObject(soloInvoice);
	}
	
	private void updateFromSoloTender(SoloTender soloTender) {
		this.tenderId = soloTender.getId();
		this.tenderNumber = soloTender.getNumber();
		updateFromSoloBillingObject(soloTender);
	}
	
	private void updateFromSoloBillingObject(SoloBillingObject soloBillingObject) {
		this.email = soloBillingObject.getEmail();
		this.paymentType = PaymentType.fromSoloPaymentType(soloBillingObject.getPaymentType());
		this.items = soloBillingObject.getProducts().stream().map(product -> new Item(product)).collect(Collectors.toList());
	}
	
	public void setReceiptSent(boolean isReceiptSent) {
		this.isReceiptSent = isReceiptSent;
	}

	@Override
	public boolean matchShopifyOrder(String shopifyOrderId) {
		return shopifyOrderId.equals(this.shopifyOrderId);
	}

	public String getEmail() {
		return email;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}
	
	public String getCurrency() {
		return currency;
	}

	public String getNote() {
		return note;
	}

	public boolean isPaid() {
		return isPaid;
	}
	
	@Override
	public void validate() {
		if (!isPaid && paymentDate != null) {
			throw new RuntimeException("Order is not yet paid");
		}
	}
	

}
