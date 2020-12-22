package org.bytepoet.shopifysolo.webinvoice.models;

import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;

public class WebInvoiceModels {

	public static WebInvoiceResponse webInvoiceResponseFiscalized() {
		WebInvoiceResponse response = webInvoiceResponseNotFiscalized();
		response.jir = "jir";
		response.qrCode = "iVBORw0KGgoAAAANSUhEUgAAAKAAAACgAQAAAACjtFqAAAABqUlEQVR4Xr2VW2oEMQwEDb6W"
				+ "wVc3+FoGp0q7kBDylZ5kyD6mDCul1a1p94erfQdefwJPa23Mxd/cp5/B7YzhvAew5mnz7lu3MRzU4W3e"
				+ "1Uaz0ngGXtrup/Xl8UNw7333Xp4+AlXgjI4Y8CPPoYP7cn1O8/dQQ1EHhScD7C+DhfB0dFhoTOdNo1Xz"
				+ "IURiFKFlXMYJ3r0pvMvGuaPpxdS4mzGEWgZ71exsP4WaVSH44GX/S5UySOPaStsqhgVTiBXa3IuvOIFA"
				+ "uBFS6A/7upUHfDEMVwZpuXyAHOitcR1cCO3dhPUSY+uKFB7XHiZzfp64V1NYgYVMp8eRsUjh0QsIiy68"
				+ "l9liSJ1tAtAZe2GGUQaL4KFllsloPlK6TrP5EKKDFyk4ZVzNkEJt6kKloinoJXIIdcIw/x6/FkwOr2ndD"
				+ "s4yih3DoQzH9pVluwEfgHT9fvqZhjJDBh2axm3KQuMWTqE55ZuhPe9iM4Y4du63vdCZjxtDHnbEFUwFML"
				+ "vlGai4xHUZtFc6YqixdJZh5b/IoRugtup9Z6ECm0G9VTltFvEREEMN9v36N/gB833Hq2OXKNQAAAAASUV"
				+ "ORK5CYII=";
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
