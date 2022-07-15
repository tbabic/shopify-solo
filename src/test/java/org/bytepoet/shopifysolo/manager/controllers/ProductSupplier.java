package org.bytepoet.shopifysolo.manager.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Transient;

import org.bytepoet.shopifysolo.manager.models.ProductWebshopInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductSupplier {
	
	
	
	
	
	public ProductWebshopInfo emptyProductWebshopInfo() {
		return new ProductWebshopInfo();
	}
	
	public ProductWebshopInfo defaultProductWebshopInfo() {
		return getProductWebshopInfo("webshop-id", 0);
	}
	
	public ProductWebshopInfo getProductWebshopInfo(String id, Integer quantity) {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.convertValue(createProductWebshopInfoMap(id, quantity), ProductWebshopInfo.class);
	}
	
	private Map<String, String> createProductWebshopInfoMap(String id, Integer quantity) {
		return Stream.of(new String[][] {
			  { "id", "Quantity" }, 
			  { "quantity", String.valueOf(quantity) }, 
			}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
	}
	
	

}
