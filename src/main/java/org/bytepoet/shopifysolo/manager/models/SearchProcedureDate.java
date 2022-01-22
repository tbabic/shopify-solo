package org.bytepoet.shopifysolo.manager.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchProcedureDate {

	@Id
	@JsonProperty
	private UUID id = UUID.randomUUID();
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	public Date date;
}
