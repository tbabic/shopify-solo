package org.bytepoet.shopifysolo.manager.models;

import java.io.IOException;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class Inventory {

	@JsonProperty
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonProperty
	@Column
	private String item;
	
	@JsonProperty
	@Column
	private int quantity;
	
	@Column
	private String linksJson;
	

	@JsonProperty
	@Transient
	public List<String> getLinks() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(
					linksJson, new TypeReference<List<String>>() { });
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonProperty
	@Transient
	public void setLinks(List<String> links) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			linksJson = mapper.writeValueAsString(links);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	
	
	
}
