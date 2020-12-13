package org.bytepoet.shopifysolo.manager.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Discount {

	@JsonProperty
	private String name;
	
	@JsonProperty
	private int percent;

	public String getName() {
		return name;
	}

	public int getPercent() {
		return percent;
	}
	
}
