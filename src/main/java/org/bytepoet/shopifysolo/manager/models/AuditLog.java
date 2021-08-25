package org.bytepoet.shopifysolo.manager.models;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class AuditLog {

	@Id
	@JsonProperty
	private UUID id = UUID.randomUUID();
	private String previousState;
	private String nextState;
	@JsonProperty
	private String changedBy;
	private Date logTime;
	
	@JsonProperty
	@Transient
	public Map<String, Object> getPrevious() {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
		try {
			return objectMapper.readValue(previousState, typeRef);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setPrevious(Object previousState) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			this.previousState = objectMapper.writeValueAsString(previousState);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@JsonProperty
	@Transient
	public Map<String, Object> getNext() {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
		try {
			return objectMapper.readValue(nextState, typeRef);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void setNext(Object nextState) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			this.nextState = objectMapper.writeValueAsString(nextState);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}
	
	
	
	
}
