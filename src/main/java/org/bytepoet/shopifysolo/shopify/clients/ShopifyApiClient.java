package org.bytepoet.shopifysolo.shopify.clients;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateTransaction;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ShopifyApiClient {
	
	private static final String ENDPOINT_FORMAT = "https://{0}/admin/api/2019-04/orders.json";
	
	private static final String TRANSACTION_ENDPOINT_FORMAT = "https://{0}/admin/api/2020-01/orders/{1}/transactions.json";
	
	private static final String SHOPIFY_DATE_PATTERN = "";
	
	@Value("${shopify.api.host}")
	private String clientHost;
	
	@Value("${shopify.api.key}")
	private String clientUsername;
	
	@Value("${shopify.api.password}")
	private String clientPassword;
	
	private static class OrdersWrapper {
		@JsonProperty
		private List<ShopifyOrder> orders;
	}

	public List<ShopifyOrder> getOrders(Boolean isPaid, Boolean isOpen, Date afterDate, Date beforeDate) throws Exception {
		String url = MessageFormat.format(ENDPOINT_FORMAT, clientHost);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		if (isPaid != null && isPaid.booleanValue()) {
			params.add("financial_status", "paid");
		}
		if (isPaid != null && !isPaid.booleanValue()) {
			params.add("financial_status", "pending");
		}
		
		if (isOpen != null && isOpen.booleanValue()) {
			params.add("status", "open");
		}
		if (isOpen != null && !isOpen.booleanValue()) {
			params.add("status", "closed");
		}
		DateFormat df = new SimpleDateFormat(SHOPIFY_DATE_PATTERN);
		if(afterDate != null) {
			params.add("created_at_min", df.format(afterDate));
		}
		if(beforeDate != null) {
			params.add("created_at_max", df.format(beforeDate));
		}
		
		OkHttpClient client = new OkHttpClient();
		
		Request request = new Request.Builder()
			      .url(buildUri(url, params))
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		OrdersWrapper wrapper = mapper.readValue(responseBodyString, OrdersWrapper.class);
		return wrapper.orders;
	}
	
	private static class TransactionsWrapper {
		@JsonProperty
		private List<ShopifyTransaction> transactions;
	}
	
	public List<ShopifyTransaction> getTransactions(String orderId) throws Exception {
		OkHttpClient client = new OkHttpClient();
		String url = MessageFormat.format(TRANSACTION_ENDPOINT_FORMAT, clientHost, orderId);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		TransactionsWrapper wrapper = mapper.readValue(responseBodyString, TransactionsWrapper.class);
		return wrapper.transactions;
	}
	
	public void createTransaction(ShopifyCreateTransaction shopifyCreateTransaction, String orderId) throws Exception {
		OkHttpClient client = new OkHttpClient();
		String url = MessageFormat.format(TRANSACTION_ENDPOINT_FORMAT, clientHost, orderId);
		
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(shopifyCreateTransaction);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.post(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not create shopify transaction: " + responseBody);
		}
	}
	
	private String buildUri(String url, MultiValueMap<String, String> params) {
	    UriComponents uriComponents = UriComponentsBuilder.newInstance()
	            .queryParams(params).build();

	   return url+uriComponents.toString();
	}
	
}
