package org.bytepoet.shopifysolo.manager.models;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class ProductWebshopInfo {

	@JsonProperty
	protected String id;
	
	@Transient
	@JsonProperty
	protected Integer quantity;
	
	@Transient
	@JsonProperty
	protected String status;
	
	
	
}
