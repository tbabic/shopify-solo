package org.bytepoet.shopifysolo.manager.models;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@DiscriminatorValue(OrderType.PAYMENT_ORDER)
@JsonTypeName(OrderType.PAYMENT_ORDER)
public class PaymentOrder extends Order {
	
	private static final int WAITING_LIST_PERIOD = 7;
	
	@JsonProperty
	@Column(unique = true)
	private String tenderId;

	@JsonProperty
	@Column(unique = true)
	private String shopifyOrderId;
	
	@JsonProperty
	private String tenderNumber;

	@JsonProperty
	private String shopifyOrderNumber;
	
	@JsonProperty
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	
	//TODO: currency
	@JsonProperty
	private String currency = "HRK";
	
	@JsonProperty
	private boolean isPaid;
	
	@JsonProperty
	private boolean isTenderSent;
	
	@Embedded
	private Invoice invoice;
	
	@Embedded
	private Invoice cancelInvoice;

	protected PaymentOrder() {
		super();
	};
	
	public PaymentOrder(ShopifyOrder shopifyOrder, GatewayToPaymentTypeMapper paymentTypeMapper, String taxRate, String shippingTitle) {
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
		
		if (!shopifyOrder.getShippingPrice().equals("0.00")) {
			items.add(new Item(shippingTitle, shopifyOrder.getShippingPrice(), 1, "0", taxRate));
		}
		this.note = shopifyOrder.getNote();
			
		
		
	}

	public void updateInvoice(Invoice invoice) {
		this.invoice = invoice;
		this.invoice.setOrder(this);
		this.isPaid = true;
		if (this.sendingDate == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, WAITING_LIST_PERIOD);
			this.sendingDate = calendar.getTime();
		}
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

	@Transient
	public Date getPaymentDate() {
		return invoice == null ? null : invoice.getDate();
	}
	
	public void setPaymentDate(Date paymentDate) {
		if (invoice != null) {
			invoice.setDate(paymentDate);
		}
	}
	
	public String getCurrency() {
		return currency;
	}

	public boolean isPaid() {
		return isPaid;
	}
	
	public boolean isReceiptSent() {
		return invoice != null && invoice.isSent();
	}
	
	public void setReceiptSent(boolean isReceiptSent) {
		if (invoice != null) {
			this.invoice.setSent(isReceiptSent);
		}
		
	}
	
	public boolean isReceiptCreated() {
		return invoice != null && invoice.getId() != null;
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

	@Transient
	public String getInvoiceId() {
		return invoice == null ? null : invoice.getId();
	}
	
	public void setInvoiceId(String invoiceId) {
		if (invoice != null) {
			invoice.setId(invoiceId);
		}
	}
	
	@Transient
	public String getInvoiceNumber() {
		return invoice == null ? null : invoice.getNumber();
	}
	
	public void setInvoiceNumber(String invoiceNumber) {
		if (invoice != null) {
			invoice.setNumber(invoiceNumber);
		}
	}
	

	@Override
	public String getShippingSnapshot() {
		return invoice == null ? null : (invoice.getNumber() + " " + contact);
	}
	
	public double getTotalPrice() {
		if (items == null) {
			return 0;
		}
		return items.stream().collect(Collectors.summingDouble(Item::getTotalPrice));
	}

	@Override
	@Transient
	public OrderType getType() {
		return OrderType.PAYMENT;
	}

	public void applyTaxRate(String taxRate) {
		if (items == null) {
			return;
		}
		for (Item item : items) {
			item.applyTaxRate(taxRate);
		}
	}

	public String getShopifyOrderNumber() {
		return shopifyOrderNumber;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public Invoice getCancelInvoice() {
		return cancelInvoice;
	}	
	
	
	
}
