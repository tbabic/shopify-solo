package org.bytepoet.shopifysolo.webinvoice.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebInvoiceResponse {
	
	public String invoiceId;
	public String invoiceLink;
	public String invoiceNumber;
	public String invoiceSequenceNumber;
	public String jir;
	public String message;
	public String zki;
	public String qrCode;
	
}
