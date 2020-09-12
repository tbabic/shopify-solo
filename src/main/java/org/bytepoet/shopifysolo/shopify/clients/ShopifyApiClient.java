package org.bytepoet.shopifysolo.shopify.clients;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateTransaction;
import org.bytepoet.shopifysolo.shopify.models.ShopifyFulfillment;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
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
	
	private static final Logger logger = LoggerFactory.getLogger(ShopifyApiClient.class);
	
	private static final String ENDPOINT_FORMAT = "https://{0}/admin/api/2019-04/orders.json";
	
	private static final String TRANSACTION_ENDPOINT_FORMAT = "https://{0}/admin/api/2020-01/orders/{1}/transactions.json";
	
	private static final String ORDER_FULFILLMENT_ENDPOINT_FORMAT = "https://{0}/admin/api/2020-01/orders/{1}/fulfillments.json";
	
	private static final String UPDATE_ORDER_FULFILLMENT_ENDPOINT_FORMAT = "https://{0}/admin/api/2020-01/orders/{1}/fulfillments/{2}.json";
	
	private static final String SHOPIFY_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	
	@Value("${fulfillment.tracking-url}")
	private String trackingUrl;
	
	@Value("${fulfillment.location-id}")
	private String locationId;
	
	@Value("${fulfillment.tracking-company}")
	private String trackingCompany;
	
	@Value("${shopify.api.host}")
	private String clientHost;
	
	@Value("${shopify.api.key}")
	private String clientUsername;
	
	@Value("${shopify.api.password}")
	private String clientPassword;
	
	@Autowired
	OkHttpClient client;

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
		
		String url = MessageFormat.format(TRANSACTION_ENDPOINT_FORMAT, clientHost, orderId);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		if (!response.isSuccessful()) {
			logger.error("Url: " + url);
			logger.error("Authorization: " + Credentials.basic(clientUsername, clientPassword));
			logger.error("Client host: " + clientHost);
			logger.error("Client username: " + clientUsername);
			logger.error("Client password: " + clientPassword);
			throw new RuntimeException("Could not fetch shopify transactions: " + responseBodyString);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		TransactionsWrapper wrapper = mapper.readValue(responseBodyString, TransactionsWrapper.class);
		return wrapper.transactions;
	}
	
	public void createTransaction(ShopifyCreateTransaction shopifyCreateTransaction, String orderId) throws Exception {
		
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
	
	private static class FulfillmentsWrapper {
		@JsonProperty
		private List<ShopifyFulfillment> fulfillments;
	}
	
	public List<ShopifyFulfillment> getFulfillments(String orderId) throws Exception {
		
		String url = MessageFormat.format(ORDER_FULFILLMENT_ENDPOINT_FORMAT, clientHost, orderId);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		if (!response.isSuccessful()) {
			logger.error("Url: " + url);
			logger.error("Authorization: " + Credentials.basic(clientUsername, clientPassword));
			logger.error("Client host: " + clientHost);
			logger.error("Client username: " + clientUsername);
			logger.error("Client password: " + clientPassword);
			throw new RuntimeException("Could not fetch shopify transactions: " + responseBodyString);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FulfillmentsWrapper wrapper = mapper.readValue(responseBodyString, FulfillmentsWrapper.class);
		return wrapper.fulfillments;
	}
	
	private static class CreateFulfillmentWrapper {
		@JsonProperty("fulfillment")
		private CreateFulfillment fulfillment;
	}
	
	private static class CreateFulfillment {
		
		@JsonProperty
		@JsonInclude(JsonInclude.Include.NON_NULL)
		private String id;
		
		@JsonProperty("location_id")
		private String locationId;
		
		@JsonProperty("tracking_number")
		private String trackingNumber;
		
		@JsonProperty("tracking_urls")
		private List<String> trackingUrls;
		
		@JsonProperty("tracking_company")
		private String trackingCompany;
		
		@JsonProperty("notify_customer")
		private boolean notifyCustomer;
	}
	
	public void fulfillOrder(String orderId, String trackingNumber, boolean notifyCustomer) throws Exception{
		
		String url = MessageFormat.format(ORDER_FULFILLMENT_ENDPOINT_FORMAT, clientHost, orderId);
		
		CreateFulfillment createFulfillment = new CreateFulfillment();
		createFulfillment.locationId = this.locationId;
		createFulfillment.trackingNumber = trackingNumber;
		createFulfillment.notifyCustomer = notifyCustomer;
		createFulfillment.trackingUrls = Arrays.asList(MessageFormat.format(trackingUrl, trackingNumber));
		createFulfillment.trackingCompany = this.trackingCompany;
		
		CreateFulfillmentWrapper createFulfillmentWrapper = new CreateFulfillmentWrapper();
		createFulfillmentWrapper.fulfillment = createFulfillment;
		
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(createFulfillmentWrapper);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.post(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not create fulfillment: " + responseBody);
		}
	}
	
	public void updateFulfillment(String orderId, String fullfillmentId, String trackingNumber, boolean notifyCustomer) throws Exception{
		
		String url = MessageFormat.format(UPDATE_ORDER_FULFILLMENT_ENDPOINT_FORMAT, clientHost, orderId, fullfillmentId);
		
		CreateFulfillment createFulfillment = new CreateFulfillment();
		createFulfillment.id = fullfillmentId;
		createFulfillment.locationId = this.locationId;
		createFulfillment.trackingNumber = trackingNumber;
		createFulfillment.notifyCustomer = notifyCustomer;
		createFulfillment.trackingUrls = Arrays.asList(MessageFormat.format(trackingUrl, trackingNumber));
		createFulfillment.trackingCompany = this.trackingCompany;
		
		CreateFulfillmentWrapper createFulfillmentWrapper = new CreateFulfillmentWrapper();
		createFulfillmentWrapper.fulfillment = createFulfillment;
		
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(createFulfillmentWrapper);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.put(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not update fulfillment: " + responseBody);
		}
	}
	
	private String buildUri(String url, MultiValueMap<String, String> params) {
	    UriComponents uriComponents = UriComponentsBuilder.newInstance()
	            .queryParams(params).build();

	   return url+uriComponents.toString();
	}
		
}
