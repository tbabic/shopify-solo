package org.bytepoet.shopifysolo.manager.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class TextRecord {
	
	@JsonProperty
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonProperty
	@Column
	private String category;
	
	@JsonProperty
	@Column
	private String value;
	
	@JsonProperty
	@Column
	private String title;
	
	@JsonProperty
	@Column
	private String extra;

	public String getCategory() {
		return category;
	}

	public String getValue() {
		return value;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	
	
	
}
