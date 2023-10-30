package org.bytepoet.shopifysolo.manager.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileData {

	@JsonProperty
	private String fileName;
	
	@JsonProperty
	private String base64Data;

	public FileData(String fileName, String base64Data) {
		this.fileName = fileName;
		this.base64Data = base64Data;
	}
}
