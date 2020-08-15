package org.bytepoet.shopifysolo.webinvoice.models;

import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;

public class WebInvoiceModels {

	public static WebInvoiceResponse webInvoiceResponseFiscalized() {
		WebInvoiceResponse response = webInvoiceResponseNotFiscalized();
		response.jir = "jir";
		return response;
		
	}
	
	public static WebInvoiceResponse webInvoiceResponseNotFiscalized() {
		WebInvoiceResponse response = new WebInvoiceResponse();
		response.invoiceId = RandomStringUtils.randomAlphanumeric(5);
		response.invoiceNumber = "1-P1-1";
		response.invoiceSequenceNumber = "1";
		response.jir = "jir";
		response.zki = "zki";
		return response;
		
	}
	
	public static WebInvoiceDetails webInvoiceDetailsFiscalized() {
		WebInvoiceDetails mock = webInvoiceDetailsNotFiscalized();
		Mockito.doReturn("jir").when(mock).getJir();
		return mock;
	}
	
	public static WebInvoiceDetails webInvoiceDetailsNotFiscalized() {
		WebInvoiceDetails mock = Mockito.mock(WebInvoiceDetails.class);
		Mockito.doReturn("1-P1-1").when(mock).getInvoiceNumber();
		Mockito.doReturn("zki").when(mock).getZki();
		Mockito.doReturn(new Date()).when(mock).getDate();
		return mock;
	}
	
}
