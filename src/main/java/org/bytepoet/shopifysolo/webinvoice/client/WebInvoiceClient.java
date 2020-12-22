package org.bytepoet.shopifysolo.webinvoice.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bytepoet.shopifysolo.webinvoice.models.WebInvoice;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceDetails;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class WebInvoiceClient {

	@Autowired
	private OkHttpClient client;
	
	@Value("${webinvoice.root-url}")
	private String rootUrl;
	
	@Value("${webinvoice.username}")
	private String username;
	
	@Value("${webinvoice.password}")
	private String password;
	
	@Value("${webinvoice.client.header}")
	private String clientHeader;
	
	@Value("${webinvoice.client.value}")
	private String clientValue;
	
	private static String token;

	public synchronized String getToken() {
		if (token == null) {
			token = login(username, password);
		}
		return token;
	}
	
	public synchronized  void invalidateToken() {
		token = null;
	}

	private String login(String username, String password) {
		try {
			Map<String, String> requestMap = new LinkedHashMap<String, String>();
			requestMap.put("username", username);
			requestMap.put("password", password);
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(requestMap);
		
			Request request = new Request.Builder()
					.url("https://b-test.com.hr/rest/api/v1/login")
					.method("POST", RequestBody.create(MediaType.parse("application/json"), body))
					.addHeader("Content-Type", "application/json")
					//.addHeader(clientHeader, clientValue)
					.build();
			Response response = client.newCall(request).execute();
			String responseBody = response.body().string();
			Map<String, String> responseMap = objectMapper.readValue(responseBody, new TypeReference<HashMap<String,String>>() {});
			return responseMap.get("token");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public WebInvoiceResponse createInvoice(String token, WebInvoice invoice) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(invoice);
			Request request = new Request.Builder()
					.url(rootUrl +"/invoice")
					.method("POST", RequestBody.create(MediaType.get("application/json"), body))
					.addHeader("Authority", token)
					.addHeader("Content-Type", "application/json")
					.addHeader(clientHeader, clientValue)
					.build();
			Response response = client.newCall(request).execute();
			String responseBody = response.body().string();
			if (response.code() == 401) {
				invalidateToken();
				throw new RuntimeException("Invalid token");
			}
			return objectMapper.readValue(responseBody, WebInvoiceResponse.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public WebInvoiceDetails getInvoiceDetails(String token, String invoiceId) {
		try {
			HttpUrl url = HttpUrl.parse(rootUrl +"/invoice").newBuilder().addQueryParameter("invoiceId", invoiceId).build();
			Request request = new Request.Builder()
					.url(url)
					.get()
					.addHeader("Authority", token)
					.addHeader(clientHeader, clientValue)
					.build();
			Response response = client.newCall(request).execute();
			String responseBody = response.body().string();
			if (response.code() == 401) {
				invalidateToken();
				throw new RuntimeException("Invalid token");
			}
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(responseBody, WebInvoiceDetails.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
