package org.bytepoet.shopifysolo.manager.models;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
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
	
	@Column
	@JsonProperty
	private String shopifyVariantId;
	
	
	
	public static class LinkContainer {
		@JsonProperty
		private String name;
		@JsonProperty
		private String link;
		
		public static LinkContainer create(String name, String link) {
			LinkContainer linkContainer = new LinkContainer();
			linkContainer.name = name;
			linkContainer.link = link;
			return linkContainer;
		}
	}
	

	@JsonProperty
	@Transient
	public List<LinkContainer> getLinks() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(
					linksJson, new TypeReference<List<LinkContainer>>() { });
		} catch (IOException e) {
			// if reading containers fails, try reading just strings;
			
			try {
				List<String> strings = mapper.readValue(
						linksJson, new TypeReference<List<String>>() { });
				return strings.stream().map(s -> {
					LinkContainer link = new LinkContainer();
					String a = s;
					String [] array = a.split(" ", 2);
					if (array.length < 0) {
						link.name = "";
						link.link = a;
					}
					else if (array.length == 1) {
						link.name = "";
						link.link = array[0];
					} else {
						link.name = array[0];
						link.link = array[1];
					}
					
					return link;
				}).collect(Collectors.toList());
			} catch (IOException e2) {
				throw new RuntimeException(e);
			}
		}
	}

	@JsonProperty
	@Transient
	public void setLinks(List<LinkContainer> links) {
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
	
	public int changeQuantity(int addQuantity) {
		this.quantity += addQuantity;
		if (this.quantity < 0) {
			this.quantity = 0;
		}
		return this.quantity;
	}
	
	
	
	
	
	
}
