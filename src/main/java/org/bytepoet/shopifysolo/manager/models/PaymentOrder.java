package org.bytepoet.shopifysolo.manager.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.shopify.models.ShopifyDiscountCode;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@DiscriminatorValue(OrderType.PAYMENT_ORDER)
@JsonTypeName(OrderType.PAYMENT_ORDER)
public class PaymentOrder extends Order {
	
	@JsonProperty
	@Column(unique = true)
	private String tenderId;

	@JsonProperty
	private String tenderNumber;
	
	@JsonProperty
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	
	@JsonProperty
	@Enumerated(EnumType.STRING)
	private Currency currency = Currency.EUR;
	
	@JsonProperty
	private boolean isPaid;
	
	@JsonProperty
	private boolean isTenderSent;
	
	@Embedded
	private Invoice invoice;
	
	@JsonProperty
	private String giftCode;

	protected PaymentOrder() {
		super();
	};
	
	public PaymentOrder(ShopifyOrder shopifyOrder, PaymentType paymentType, String taxRate) {
		if (shopifyOrder == null) {
			throw new RuntimeException("Shopify order can not be null");
		}
		this.creationDate = new Date();
		
		this.shopifyOrderId = shopifyOrder.getId();
		this.shopifyOrderNumber = shopifyOrder.getNumber();
		this.shippingInfo = new Address(shopifyOrder.getShippingAddress());
		this.contact = shopifyOrder.getEmail();
		this.creationDate = shopifyOrder.getCreated();
		this.paymentType = paymentType;
		this.contact = shopifyOrder.getEmail();
		this.items = shopifyOrder.getLineItems().stream().map(lineItem -> new Item(lineItem, taxRate)).collect(Collectors.toList());
		
		if (StringUtils.isNotBlank(shopifyOrder.getShippingTitle())) {
			Item item = new Item(shopifyOrder.getShippingTitle(), shopifyOrder.getShippingPrice(), 1, "0", taxRate);
			item.setShipping(true);
			items.add(item);
			this.shippingType = ShippingType.valueOfShippingTittle(shopifyOrder.getShippingTitle());
		}
		this.note = shopifyOrder.getNote();
		if(this.getTotalPrice() >= 500.0) {
			this.note += "\nPoslati bon od 50 kn";
		}
	}
	
	public PaymentOrder(ShopifyOrder shopifyOrder, GatewayToPaymentTypeMapper paymentTypeMapper, String taxRate, String giftCodeType) {
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
		
		if (StringUtils.isNotBlank(shopifyOrder.getShippingTitle())) {
			Item item = new Item(shopifyOrder.getShippingTitle(), shopifyOrder.getShippingPrice(), 1, "0", taxRate);
			item.setShipping(true);
			items.add(item);
			this.shippingType = ShippingType.valueOfShippingTittle(shopifyOrder.getShippingTitle());
			
		}
		

		this.note = shopifyOrder.getNote();
		if(this.getTotalPrice() >= 500.0) {
			this.note += "\nPoslati bon od 50 kn";
		}
		
		List<String> giftCodes = new ArrayList<>();
		for (ShopifyDiscountCode discountCode : shopifyOrder.getDiscountCodes()) {
			if (discountCode.getType().equalsIgnoreCase(giftCodeType)) {
				giftCodes.add(discountCode.getCode());
			}
		}
		this.giftCode = String.join(";", giftCodes);
		
		
		
	}
	
	

	public void updateInvoice(Invoice invoice) {
		this.invoice = invoice;
		this.invoice.setOrder(this);
		this.isPaid = true;
		
		if (this.sendingDate != null) {
			return;
		}
		

		if (isGiftCode() || isPriorityShipping()) {
			this.sendingDate = invoice.getDate();
		}
		
		else {
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
		if (invoice == null) {
			invoice = new Invoice.Builder().build();
		}
		if (invoice != null) {
			invoice.setDate(paymentDate);
		}
	}
	
	public Currency getCurrency() {
		return currency;
	}

	public boolean isPaid() {
		return isPaid;
	}
	
	public boolean isReceiptSent() {
		return invoice != null && invoice.isSent();
	}
	
	public void setReceiptSent(boolean isReceiptSent) {
		if (invoice == null) {
			invoice = new Invoice.Builder().build();
		}
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
	@JsonProperty
	public String getInvoiceId() {
		return invoice == null ? null : invoice.getId();
	}
	
	public void setInvoiceId(String invoiceId) {
		if (invoice == null) {
			invoice = new Invoice.Builder().build();
		}
		if (invoice != null) {
			invoice.setId(invoiceId);
		}
	}
	
	@Transient
	@JsonProperty
	public String getInvoiceNumber() {
		return invoice == null ? null : invoice.getNumber();
	}
	
	public void setInvoiceNumber(String invoiceNumber) {
		if (invoice == null) {
			invoice = new Invoice.Builder().build();
		}
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
	
	public double getTotalPrice(Currency currency) {
		return currency.convertFrom(this.currency, this.getTotalPrice());
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

	public Invoice getInvoice() {
		return invoice;
	}
	
	public Refund createRefund(List<Long> itemsIds) {
		List<Item> refundedItems = items.stream()
				.filter(item -> itemsIds.contains(item.getId()) && !item.isRefunded())
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(refundedItems)) {
			throw new RuntimeException("No items to refund");
		}
		if (refundedItems.size() == items.size()) {
			super.status = OrderStatus.REFUNDED;
		}
		return new Refund(this, refundedItems);
	}
	
	@JsonIgnore
	public List<String> getGiftCodes() {
		if (StringUtils.isBlank(this.giftCode)) {
			return Collections.emptyList();
		}
		return Arrays.asList(this.giftCode.split(";"));
	}
	
}
