package org.bytepoet.shopifysolo.manager.models;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Parent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Embeddable
public class SearchProcedureHistory {
	
	@Parent
	@JsonIgnore
	private Order order;
	
	@JsonIgnore
	private String json;
	
	
	
	@JsonProperty
	@Transient
	private Map<String, TrackingNumberHistory> trackingNumbers;
	
	public static class TrackingNumberHistory {
		
		@JsonProperty
		private Map<SearchProcedureStatus, Date> procedureHistory;
		@JsonProperty
		private Map<ShippingSearchStatus, Date> shippingHistory;
		
	}
	
	public boolean exists() {
		return StringUtils.isNotBlank(json); 
	}

	public void addStatus(String trackingNumber, SearchProcedureStatus status, Date date) {
		TrackingNumberHistory history = insertAndGetTrackingNumberHistory(trackingNumber);
		if (history.procedureHistory == null && status == SearchProcedureStatus.NONE) {
			return;
		}
		if (history.procedureHistory == null) {
			history.procedureHistory = new LinkedHashMap<SearchProcedureStatus, Date>();
		}
		history.procedureHistory.put(status,date);
		json = toString(trackingNumbers);
	}
	
	public void addStatus(String trackingNumber, ShippingSearchStatus status, Date date) {
		TrackingNumberHistory history = insertAndGetTrackingNumberHistory(trackingNumber);
		if (history.shippingHistory == null && status == ShippingSearchStatus.NONE) {
			return;
		}
		if (history.shippingHistory == null) {
			history.shippingHistory = new LinkedHashMap<ShippingSearchStatus, Date>();
		}
		history.shippingHistory.put(status, date);
		json = toString(trackingNumbers);
	}
	
	
	public Map<String, TrackingNumberHistory> getTrackingNumbers() {
		if (StringUtils.isBlank(json)) {
			trackingNumbers = null;
		}
		else if (trackingNumbers == null) {
			trackingNumbers = fromString(json);
		}
		return trackingNumbers;
	}

	public void setTrackingNumbers(Map<String, TrackingNumberHistory> trackingNumbers) {
		this.trackingNumbers = trackingNumbers;
		this.json = toString(this.trackingNumbers);
	}
	
	private TrackingNumberHistory insertAndGetTrackingNumberHistory(String trackingNumber) {
		if(this.trackingNumbers == null && StringUtils.isNotBlank(json)) {
			this.trackingNumbers = fromString(json);
		}
		if (this.trackingNumbers == null) {
			this.trackingNumbers = new LinkedHashMap<String, TrackingNumberHistory>();
		}
		
		TrackingNumberHistory history = trackingNumbers.get(trackingNumber);
		if (history == null) {
			history = new TrackingNumberHistory();
			trackingNumbers.put(trackingNumber, history);
		}
		return history;
	}
	
	
	static Map<String, TrackingNumberHistory> fromString(String string) {
		if (StringUtils.isBlank(string)) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, TrackingNumberHistory> trackingNumbers = mapper.readValue(string, new TypeReference<Map<String, TrackingNumberHistory>>() { });
			return trackingNumbers;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	static String toString(Map<String, TrackingNumberHistory> trackingNumbers) {
		if (trackingNumbers == null) {
			return null;
		}
 		ObjectMapper mapper = new ObjectMapper();
    	try {
			return mapper.writeValueAsString(trackingNumbers);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
    	
	}

	@JsonIgnore
	public void setOrder(Order order) {
		this.order = order;
	}
	
	@JsonIgnore
	public Order getOrder() {
		return order;
	}
	

}
