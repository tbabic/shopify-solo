package org.bytepoet.shopifysolo.manager.models;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Refund {

	@JsonProperty
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Embedded
	private RefundInvoice invoice;
	
	@OneToMany(mappedBy = "refund", cascade=CascadeType.ALL)
	private List<Item> items;
	
	@ManyToOne
	@JoinColumn(name = "orderId")
	private PaymentOrder order;
	
	
	public Refund() {
	}
	
	public Refund(PaymentOrder order, List<Item> items ) {
		this.items = items;
		this.items.stream().forEach(item -> item.setRefund(this));
		this.order = order;
	}

	public RefundInvoice getInvoice() {
		return invoice;
	}

	public void setInvoice(RefundInvoice invoice) {
		this.invoice = invoice;
	}

	public List<Item> getItems() {
		return items;
	}

	public PaymentOrder getOrder() {
		return order;
	}

	public Long getId() {
		return id;
	}
	
	void setId(Long id) {
		this.id = id;
	}

	public double getTotalPrice() {
		if (items == null) {
			return 0;
		}
		return items.stream().collect(Collectors.summingDouble(Item::getTotalPrice));
	}

	
	
	
	
}

