package org.bytepoet.shopifysolo.manager.models;

import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.EXISTING_PROPERTY, 
		  property = "type")
@JsonSubTypes({ 
  @Type(value = GiveawayOrder.class, name = OrderType.GIVEAWAY_ORDER), 
  @Type(value = PaymentOrder.class, name = OrderType.PAYMENT_ORDER) 
})
@Entity(name="ManagedOrder")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type", discriminatorType = DiscriminatorType.STRING)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Order {
	
	
	@JsonProperty
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonProperty
	@Column(unique = true)
	protected String shopifyOrderId;
	
	@JsonProperty
	protected String shopifyOrderNumber;
	
	@JsonProperty
	protected String contact;
	
	@JsonProperty
	@Embedded
	protected Address shippingInfo;
	
	@JsonProperty
	protected boolean personalTakeover;
	
	@JsonProperty
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "orderId", referencedColumnName = "id", nullable = false, insertable=true, updatable=false)
	protected List<Item> items;	
	
	//TODO: dates
	@JsonProperty
	protected Date creationDate;
	
	@JsonProperty
	protected Date sendingDate;
	
	@JsonProperty
	private boolean isFulfilled;
	
	@JsonProperty
	@Enumerated(EnumType.STRING)
	protected OrderStatus status = OrderStatus.INITIAL;
	
	@JsonProperty
	@Enumerated(EnumType.STRING)
	private ShippingSearchStatus shippingSearchStatus = ShippingSearchStatus.NONE;
	
	@JsonProperty
	private String trackingNumber;
	
	@JsonProperty
	private boolean isCanceled;
	
	@JsonProperty
	protected String note;

	public static enum Type {
		
		GIVEAWAY,
		PAYMENT
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String getShopifyOrderId() {
		return shopifyOrderId;
	}
	
	public List<Item> getItems() {
		return items;
	}

	public boolean isFulfilled() {
		return isFulfilled;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getSendingDate() {
		return sendingDate;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public String getContact() {
		return contact;
	}
	
	public String getTrackingNumber() {
		return trackingNumber;
	}
	
	@Transient
	@JsonProperty(access = Access.READ_ONLY)
	public abstract String getShippingSnapshot();
	
	@Transient
	public abstract OrderType getType();
	
	
	public void fulfill(String trackingNumber) {
		this.status = OrderStatus.FULFILLED;
		this.isFulfilled = true;
		if (!this.personalTakeover) {
			this.trackingNumber = trackingNumber;
		} 
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public void addNote(String note) {
		this.note += note;
	}

	public boolean isPersonalTakeover() {
		return personalTakeover;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public Address getShippingInfo() {
		return shippingInfo;
	}
	
	@PrePersist
    public void onPrePersist() {
		if (this.getCreationDate() == null) {
			this.creationDate = new Date();
		}
	}
	
	@JsonProperty(access = Access.READ_ONLY)
	public double getWeight() {
		double weight = 0;
		double letterWeight = 5;
		double itemBoxWeight = 6;
		int count = 0;
		for (Item item : this.items) {
			if (item.getName().toLowerCase().contains("poštarina")) {
				continue;
			}
			double itemWeight = item.getWeight();
			if (itemWeight == 0) {
				return 0;
			}
			if (!item.getName().toLowerCase().contains("poklon bon")) {
				letterWeight = 60;
				itemWeight+=itemBoxWeight;
			}
			weight+=itemWeight;
			count++;
		}
		if (count > 3) {
			letterWeight = 60;
		}
		weight += letterWeight;
		
		return weight;
	}
	
	public boolean updateFromShopify(ShopifyOrder shopifyOrder) {
		boolean orderSuccess = true;
		for (Item item : this.items) {
			for (ShopifyLineItem lineItem : shopifyOrder.getLineItems()) {
				if (item.getName().equalsIgnoreCase(lineItem.getFullTitle())) {
					boolean itemSuccess = item.updateFromShopify(lineItem);
					if(!itemSuccess) {
						orderSuccess = false;
					}
				}
			}
		}
		return orderSuccess;
	}
	
}
