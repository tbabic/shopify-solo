package org.bytepoet.shopifysolo.manager.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class OrderArchive {

	@JsonProperty
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Lob
	private String data;
	
	@JsonProperty
	private Date starting;
	
	@JsonProperty
	private Date ending;
	
	@Transient
	@JsonProperty
	private List<Order> orders;
	
	public List<Order> getOrders() {
		if (orders == null && data != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				orders = mapper.readValue(
						data, new TypeReference<List<Order>>() { });
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (orders == null) {
			orders = new ArrayList<>();
		}
		return orders;
	}
	
	public void addOrder(Order order) {
		getOrders().add(order);
	}
	
	public void updateData() {
    	if (orders == null) {
    		data = null;
    		return;
    	}
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			data = mapper.writeValueAsString(orders);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
    	
    	
    }
	
	
	@PrePersist
    public void onPrePersist() {
		updateData();
	}
       
    @PreUpdate
    public void onPreUpdate() {
    	updateData();
    }
    
    
	
	
}
