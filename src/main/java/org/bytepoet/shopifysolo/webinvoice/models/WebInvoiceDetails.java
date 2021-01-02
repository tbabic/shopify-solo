package org.bytepoet.shopifysolo.webinvoice.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebInvoiceDetails {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Details {
		
		@JsonProperty("racunDto")
		private Dto dto;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Dto {
		
		@JsonProperty("datumRacun")
		private String date;
		
		@JsonProperty("jir")
		private String jir;
		
		@JsonProperty("zki")
		private String zki;
		
		@JsonProperty("brojRacun")
		private String invoiceSequence;
		
		@JsonProperty("poslovniProstorOznaka")
		private String locationNumber;
		
		@JsonProperty("naplatniUredjajOznaka")
		private String deviceNumber;
		
	}
	
	@JsonProperty("racunResult")
	private Details details;
	
	
	
	public String getJir() {
		return details.dto.jir;
	}
	
	public String getZki() {
		return details.dto.zki;
	}
	
	public Date getDate() {
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("CET"));
		try {
			return df.parse(details.dto.date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getInvoiceNumber() {
		return details.dto.invoiceSequence + "-" + details.dto.locationNumber + "-" + details.dto.deviceNumber;
	}
}
