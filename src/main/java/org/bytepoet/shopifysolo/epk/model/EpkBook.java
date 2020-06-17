package org.bytepoet.shopifysolo.epk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EpkBook {

	@JsonProperty
	private String fileName;
	
	@JsonProperty
	private String base64Data;

	public EpkBook(String fileName, String base64Data) {
		this.fileName = fileName;
		this.base64Data = base64Data;
	}
	
	
	
}
