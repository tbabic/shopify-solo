package org.bytepoet.shopifysolo.manager.models;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.bytepoet.shopifysolo.solo.models.SoloTender;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@DiscriminatorValue(OrderType.PAYMENT_ORDER)
public class PaymentOrder extends Order {
	
	private static final int WAITING_LIST_PERIOD = 7;
	
	@JsonProperty(access = Access.READ_ONLY)
	@Column(unique = true)
	private String tenderId;
	@JsonProperty(access = Access.READ_ONLY)
	@Column(unique = true)
	private String invoiceId;
	@JsonProperty(access = Access.READ_ONLY)
	@Column(unique = true)
	private String shopifyOrderId;
	
	@JsonProperty(access = Access.READ_ONLY)
	private String tenderNumber;
	@JsonProperty(access = Access.READ_ONLY)
	private String invoiceNumber;
	@JsonProperty(access = Access.READ_ONLY)
	private String shopifyOrderNumber;
	
	@JsonProperty
	@Enumerated(EnumType.STRING)
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
	
	@JsonProperty
	private boolean isTenderSent;

	protected PaymentOrder() {
		super();
	};
	
	public PaymentOrder(ShopifyOrder shopifyOrder, GatewayToPaymentTypeMapper paymentTypeMapper, String taxRate) {
		if (shopifyOrder == null) {
			throw new RuntimeException("Shopify order can not be null");
		}
		this.creationDate = new Date();
		
		this.shopifyOrderId = shopifyOrder.getId();
		this.shopifyOrderNumber = shopifyOrder.getNumber();
		this.shippingInfo = new Address(shopifyOrder.getShippingAddress());
		this.contact = shopifyOrder.getEmail();
		this.creationDate = shopifyOrder.getCreated();
		this.paymentType = paymentTypeMapper.getPaymentType(shopifyOrder);
		this.contact = shopifyOrder.getEmail();
		this.items = shopifyOrder.getLineItems().stream().map(lineItem -> new Item(lineItem, taxRate)).collect(Collectors.toList());
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
	
	public void updateFromSoloTender(SoloTender soloTender) {
		this.tenderId = soloTender.getId();
		this.tenderNumber = soloTender.getNumber();
		updateFromSoloBillingObject(soloTender);
	}
	
	private void updateFromSoloBillingObject(SoloBillingObject soloBillingObject) {
		this.paymentType = PaymentType.fromSoloPaymentType(soloBillingObject.getPaymentType());
	}

	@Transient
	public String getEmail() {
		return contact;
	}

	public String getShopifyOrderId() {
		return shopifyOrderId;
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
	
	public boolean isReceiptSent() {
		return isReceiptSent;
	}
	
	public void setReceiptSent(boolean isReceiptSent) {
		this.isReceiptSent = isReceiptSent;
	}
	
	public boolean isReceiptCreated() {
		return invoiceId != null;
	}
	
	public boolean isTenderSent() {
		return isTenderSent;
	}
	
	public void setTenderSent(boolean isTenderSent) {
		this.isTenderSent = isTenderSent;
	}

	public boolean isTenderCreated() {
		return tenderId != null;
	}

	public String getTenderId() {
		return tenderId;
	}

	public String getInvoiceId() {
		return invoiceId;
	}	
	
}
