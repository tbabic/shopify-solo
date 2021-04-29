package org.bytepoet.shopifysolo.services;

import java.util.Calendar;
import java.util.TimeZone;

import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class StatementsService {

	@Value("${statements.email}")
	private String user;
	
	@Value("${statements.password}")
	private String password;
	
	
	@Autowired
	private OkHttpClient client;
	
	public void readEmail() {
		
	}
	
	
	private void readEtsyInvoices() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CET"));
		cal.set(Calendar.DATE, 1);
		
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		cal.add(Calendar.MONTH, -1);
		
		long startTime = cal.getTimeInMillis();
		
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.MILLISECOND, -1);
		
		long endTime = cal.getTimeInMillis();
		
		
		String query = "after:"+startTime+ "before:"+endTime+ "transaction@etsy.com ";
		
	
		
		HttpUrl url = HttpUrl.parse("https://www.googleapis.com/gmail/v1/users/me/messages").newBuilder().addQueryParameter("q", query).build();
		Request request = new Request.Builder()
				.url(url)
				.get()
				.build();
		try {
			Response response = client.newCall(request).execute();
			String responseBody = response.body().string();
			return;
		} catch(Exception e) {
			throw new RuntimeException();
		}
		
		
		
		
		
		
	}
}
