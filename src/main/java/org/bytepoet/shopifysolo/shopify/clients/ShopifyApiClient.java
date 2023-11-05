package org.bytepoet.shopifysolo.shopify.clients;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.ShippingType;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollect;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollection;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollectionCustomUpdate;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateDiscount;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateDraftOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreatePriceRule;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateTransaction;
import org.bytepoet.shopifysolo.shopify.models.ShopifyDiscountCode;
import org.bytepoet.shopifysolo.shopify.models.ShopifyFulfillment;
import org.bytepoet.shopifysolo.shopify.models.ShopifyInventoryAdjustment;
import org.bytepoet.shopifysolo.shopify.models.ShopifyLocation;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyPriceRule;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import org.bytepoet.shopifysolo.shopify.models.ShopifyTransaction;
import org.bytepoet.shopifysolo.shopify.models.ShopifyUpdateVariantRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.bytebuddy.asm.Advice.This;
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
	
	private static final String ORDER_FORMAT = "https://{0}/admin/api/2019-04/orders/{1}.json";
	
	private static final String TRANSACTION_ENDPOINT_FORMAT = "https://{0}/admin/api/2020-01/orders/{1}/transactions.json";
	
	private static final String FULFILLMENT_ENDPOINT_FORMAT = "https://{0}/admin/api/2023-07/fulfillments.json";
	
	private static final String FULFILLMENT_ORDERS_ENDPOINT_FORMAT = "https://{0}/admin/api/2020-01/orders/{1}/fulfillment_orders.json";
	
	private static final String ORDER_FULFILLMENT_ENDPOINT_FORMAT = "https://{0}/admin/api/2020-01/orders/{1}/fulfillments.json";
	
	private static final String UPDATE_ORDER_FULFILLMENT_ENDPOINT_FORMAT = "https://{0}/admin/api/2023-07/fulfillments/{1}/update_tracking.json";
	
	private static final String PRICE_RULES_FORMAT = "https://{0}/admin/api/2020-10/price_rules.json";
	
	private static final String PRICE_RULE_FORMAT = "https://{0}/admin/api/2020-10/price_rules/{1}.json";
	
	private static final String DISCOUNT_FORMAT = "https://{0}/admin/api/2020-10/price_rules/{1}/discount_codes.json";
	
	private static final String PRODUCTS_FORMAT = "https://{0}/admin/api/2021-04/products.json";
	
	public static final String SHOPIFY_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	
	private static final String DRAFT_ORDER_FORMAT = "https://{0}/admin/api/2019-04/draft_orders.json";
	
	private static final String COMPLETE_DRAFT_ORDER_FORMAT = "https://{0}/admin/api/2019-04/draft_orders/{1}/complete.json?payment_pending=true";
	
	private static final String UPDATE_PRODUCT_VARIANT = "https://{0}/admin/api/2021-01/variants/{1}.json";
	
	private static final String DISCOUNT_CODE_LOOKUP = "https://{0}/admin/api/2021-04/discount_codes/lookup.json?code={1}";
	
	private static final String SHOPIFY_SMART_COLLECTIONS="https://{0}/admin/api/2021-10/smart_collections.json?limit=250";
	
	private static final String SHOPIFY_CUSTOM_COLLECTIONS="https://{0}/admin/api/2021-10/custom_collections.json?limit=250";
	
	private static final String SHOPIFY_COLLECTION_PRODUCTS="https://{0}/admin/api/2021-10/collections/{1}/products.json?limit=250";
	
	private static final String SHOPIFY_CUSTOM_COLLECTION="https://{0}/admin/api/2021-10/custom_collections/{1}.json";

	private static final String SHOPIFY_SMART_COLLECTION_ORDER="https://{0}/admin/api/2021-10/smart_collections/{1}/order.json";
	
	private static final String SHOPIFY_COLLECTS= "https://{0}/admin/api/2021-10/collects.json";
	
	private static final String SHOPIFY_VARIANT = "https://{0}/admin/api/2021-10/variants/{1}.json";
	
	private static final String SHOPIFY_PRODUCT = "https://{0}/admin/api/2021-10/products/{1}.json";
	
	private static final String SHOPIFY_ADJUST_INVENTORY = "https://{0}/admin/api/2022-07/inventory_levels/adjust.json";
	
	private static final String LOCATIONS_FORMAT = "https://{0}/admin/api/2021-10/locations.json";
	
	@Value("${fulfillment.hp.tracking-url}")
	private String hpTrackingUrl;
	
	@Value("${fulfillment.hp.location-id}")
	private String hpLocationId;
	
	@Value("${fulfillment.hp.tracking-company}")
	private String hpTrackingCompany;
	
	@Value("${fulfillment.gls.tracking-url}")
	private String glsTrackingUrl;
	
	@Value("${fulfillment.gls.location-id}")
	private String glsLocationId;
	
	@Value("${fulfillment.gls.tracking-company}")
	private String glsTrackingCompany;
	
	@Value("${shopify.api.host}")
	private String clientHost;
	
	@Value("${shopify.api.key}")
	private String clientUsername;
	
	@Value("${shopify.api.password}")
	private String clientPassword;
	
	
	
	@Autowired
	private OkHttpClient client;
	
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
	
	
	private static class OrderWrapper {
		@JsonProperty
		private ShopifyOrder order;
	}
	
	public ShopifyOrder getOrder(String orderId) throws Exception {
		String url = MessageFormat.format(ORDER_FORMAT, clientHost, orderId);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		OrderWrapper wrapper = mapper.readValue(responseBodyString, OrderWrapper.class);
		return wrapper.order;
	}
	
	private static class CreateOrderWrapper {
		@JsonProperty
		private ShopifyCreateOrder order;
	}
	
	public ShopifyOrder createOrder(ShopifyCreateOrder order) throws Exception {
		String url = MessageFormat.format(ENDPOINT_FORMAT, clientHost);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		CreateOrderWrapper requestWrapper = new CreateOrderWrapper();
		requestWrapper.order = order;
		String requestBody = mapper.writeValueAsString(requestWrapper);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.post(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not create order: " + responseBody);
		}
		OrderWrapper resp = mapper.readValue(responseBody, OrderWrapper.class);
		return resp.order;
		
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
	
	public static class FulfillmentOrderId {
		@JsonProperty
		public String id;
	}
	
	private static class FulfillmentOrdersWrapper {
		@JsonProperty("fulfillment_orders")
		private List<FulfillmentOrderId> fulfillments;
	}
	
	public List<FulfillmentOrderId> getFulfillmentOrders(String orderId)  throws Exception {
		String url = MessageFormat.format(FULFILLMENT_ORDERS_ENDPOINT_FORMAT, clientHost, orderId);
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
		FulfillmentOrdersWrapper wrapper = mapper.readValue(responseBodyString, FulfillmentOrdersWrapper.class);
		return wrapper.fulfillments;
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
	
	private static class FulfillmentOrder {
		
		@JsonProperty("fulfillment_order_id")
		private String id;
	}
	
	private static class FulfillmentTracking {
		
		@JsonProperty("number")
		private String trackingNumber;
		
		@JsonProperty("url")
		private String trackingUrl;
		
		@JsonProperty("company")
		private String trackingCompany;
	}
	
	private static class CreateFulfillment {
		
		@JsonProperty
		@JsonInclude(JsonInclude.Include.NON_NULL)
		private String id;
		
		@JsonProperty("line_items_by_fulfillment_order")
		private List<FulfillmentOrder> orders;

		@JsonProperty("tracking_info")
		private FulfillmentTracking tracking;

		@JsonProperty("notify_customer")
		private boolean notifyCustomer;
	}
	
	public void fulfillOrder(String orderId, String trackingNumber, ShippingType shippingType, boolean notifyCustomer) throws Exception{
		
		List<FulfillmentOrderId> fulfillmentOrderIds = this.getFulfillmentOrders(orderId);
		String url = MessageFormat.format(FULFILLMENT_ENDPOINT_FORMAT, clientHost);
		
		CreateFulfillment createFulfillment = new CreateFulfillment();
		FulfillmentOrder fullfillmentOrder = new FulfillmentOrder();
		fullfillmentOrder.id = fulfillmentOrderIds.get(0).id;
		createFulfillment.orders = Arrays.asList(fullfillmentOrder);

		FulfillmentTracking tracking = new FulfillmentTracking();
		tracking.trackingNumber = trackingNumber;
		if (shippingType == ShippingType.HP_REGISTERED_MAIL) {
			tracking.trackingUrl = MessageFormat.format(hpTrackingUrl, trackingNumber);
			tracking.trackingCompany = this.hpTrackingCompany;
		}
		else {
			tracking.trackingUrl = MessageFormat.format(glsTrackingUrl, trackingNumber);
			tracking.trackingCompany = this.glsTrackingCompany;
		}
		
		
		createFulfillment.tracking = tracking;
		createFulfillment.notifyCustomer = notifyCustomer;
		
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
	
	public void updateFulfillment(String orderId, String fullfillmentId, String trackingNumber, ShippingType shippingType, boolean notifyCustomer) throws Exception{
		
		List<FulfillmentOrderId> fulfillmentOrderIds = this.getFulfillmentOrders(orderId);
		String url = MessageFormat.format(UPDATE_ORDER_FULFILLMENT_ENDPOINT_FORMAT, clientHost, fullfillmentId);
		
		CreateFulfillment createFulfillment = new CreateFulfillment();
		FulfillmentOrder fullfillmentOrder = new FulfillmentOrder();
		fullfillmentOrder.id = fulfillmentOrderIds.get(0).id;
		createFulfillment.orders = Arrays.asList(fullfillmentOrder);

		FulfillmentTracking tracking = new FulfillmentTracking();
		tracking.trackingNumber = trackingNumber;
		if (shippingType == ShippingType.HP_REGISTERED_MAIL) {
			tracking.trackingUrl = MessageFormat.format(hpTrackingUrl, trackingNumber);
			tracking.trackingCompany = this.hpTrackingCompany;
		}
		else {
			tracking.trackingUrl = MessageFormat.format(glsTrackingUrl, trackingNumber);
			tracking.trackingCompany = this.glsTrackingCompany;
		}
		
		createFulfillment.tracking = tracking;
		createFulfillment.notifyCustomer = notifyCustomer;
		
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
	
	public String createPriceRule(ShopifyCreatePriceRule priceRule) throws Exception	{
		String url = MessageFormat.format(PRICE_RULES_FORMAT, clientHost);
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(priceRule);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.post(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not create price rule: " + responseBody);
		}
		ShopifyPriceRule priceRuleResponse = mapper.readValue(responseBody, ShopifyPriceRule.class);
		return priceRuleResponse.getPriceRule().getId();
	}
	
	public void createDiscount(String priceRuleId, ShopifyCreateDiscount discountCode) throws Exception	{
		String url = MessageFormat.format(DISCOUNT_FORMAT, clientHost, priceRuleId);
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(discountCode);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.post(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not create discount: " + responseBody);
		}
	}
	
	public static class ProductsWrapper {
		
		@JsonProperty
		private List<ShopifyProduct> products;
	}
	
	public List<ShopifyProduct> getProducts(String title, int limit) throws Exception {
		String url = MessageFormat.format(PRODUCTS_FORMAT, clientHost);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		if (StringUtils.isNotBlank(title)) {
			params.add("title", title);
		}
		params.add("limit", Integer.toString(limit));
		
		
		Request request = new Request.Builder()
			      .url(buildUri(url, params))
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ProductsWrapper wrapper = mapper.readValue(responseBodyString, ProductsWrapper.class);
		return wrapper.products;
	}
	
	public List<ShopifyProduct> getProducts(String title) throws Exception {
		String url = MessageFormat.format(PRODUCTS_FORMAT, clientHost);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		if (StringUtils.isNotBlank(title)) {
			params.add("title", title);
		}
		
		
		Request request = new Request.Builder()
			      .url(buildUri(url, params))
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ProductsWrapper wrapper = mapper.readValue(responseBodyString, ProductsWrapper.class);
		
		List<ShopifyProduct> products = new ArrayList<>(wrapper.products);
		
		Map<String, String> links = Stream.of(response.header("link").split(",")).collect(Collectors.toMap(
				h -> h.split(";")[1].trim(),
				h -> h.split(";")[0].replace('<',' ').replace('>', ' ').trim()));
		String nextUrl = links.get("rel=\"next\"");
		
		while (nextUrl != null) {
			request = new Request.Builder()
				      .url(nextUrl)
				      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				      .build();
			response = client.newCall(request).execute();
			responseBodyString = response.body().string();
			wrapper = mapper.readValue(responseBodyString, ProductsWrapper.class);
			products.addAll(wrapper.products);
			
			links = Stream.of(response.header("link").split(",")).collect(Collectors.toMap(
					h -> h.split(";")[1].trim(),
					h -> h.split(";")[0].replace('<',' ').replace('>', ' ').trim()));
			nextUrl = links.get("rel=\"next\"");
		}
		
		return products;
	}
	
	
	public static class DraftOrderWrapper {
		
		@JsonProperty("draft_order")
		private ShopifyCreateDraftOrder draft;
	}
	
	public static class DraftResponse {
		
		@JsonProperty
		private String id;
	
		@JsonProperty("order_id")
		private String orderId;
	}
	
	
	public static class DraftResponseWrapper {
		
		@JsonProperty("draft_order")
		private DraftResponse draft;
	}
	
	public String createDraftOrder(ShopifyCreateDraftOrder order) throws Exception {
		String url = MessageFormat.format(DRAFT_ORDER_FORMAT, clientHost);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		DraftOrderWrapper requestWrapper = new DraftOrderWrapper();
		requestWrapper.draft = order;
		String requestBody = mapper.writeValueAsString(requestWrapper);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.post(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not create draft order: " + responseBody);
		}
		DraftResponseWrapper resp = mapper.readValue(responseBody, DraftResponseWrapper.class);
		return resp.draft.id;
		
	}
	
	public String completeDraftOrder(String draftId) throws Exception {
		String url = MessageFormat.format(COMPLETE_DRAFT_ORDER_FORMAT, clientHost, draftId);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.put(RequestBody.create(null, new byte[] {}))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not create complete draft: " + responseBody);
		}
		DraftResponseWrapper resp = mapper.readValue(responseBody, DraftResponseWrapper.class);
		return resp.draft.orderId;
		
	}
	
	public static class UpdateVariantWrapper {
		
		@JsonProperty
		private ShopifyUpdateVariantRequest variant;
	}
	
	public void updateProductVariant(String variantId, ShopifyUpdateVariantRequest variantRequest) throws Exception {
		String url = MessageFormat.format(UPDATE_PRODUCT_VARIANT, clientHost, variantId);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		UpdateVariantWrapper requestWrapper = new UpdateVariantWrapper();
		requestWrapper.variant = variantRequest;
		String requestBody = mapper.writeValueAsString(requestWrapper);
		
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.put(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not update variant: " + responseBody);
		}
		
	}
	
	public static class ShopifyUpdateProductBody {
		@JsonProperty("body_html")
		public String bodyHtml;
}
	
	public static class ShopifyUpdateProductBodyWrapper {
		@JsonProperty
		public ShopifyUpdateProductBody product = new ShopifyUpdateProductBody();
	}
	

	
	public void updateProductBody(String productId, String bodyHtml) throws Exception {
		String url = MessageFormat.format(SHOPIFY_PRODUCT, clientHost, productId);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ShopifyUpdateProductBodyWrapper requestObject = new ShopifyUpdateProductBodyWrapper();
		requestObject.product.bodyHtml = bodyHtml;
		String requestBody = mapper.writeValueAsString(requestObject);
		
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.put(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not update variant: " + responseBody);
		}
		
	}

	public ShopifyPriceRule getDiscountPriceRule(String discountCode) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		
		String url = MessageFormat.format(DISCOUNT_CODE_LOOKUP, clientHost, discountCode);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String priceRuleId = null;
		if (response.code() == 303) {
			String location = response.header("location");
			priceRuleId = location.split("price_rules/")[1].split("/")[0];
			
		} else if (response.isSuccessful()) {
			String responseBody = response.body().string();
			TypeReference<Map<String, Map<String, String>>> typeRef = new TypeReference<Map<String, Map<String, String>>>() {};
			Map<String, Map<String, String>> wrapper = mapper.readValue(responseBody, typeRef);
			priceRuleId = wrapper.get("discount_code").get("price_rule_id");
		} else {
			throw new RuntimeException("could not fetch code" + response.body().string());
		}
		
		
		if (priceRuleId == null) {
			throw new RuntimeException("could not find code:" + discountCode);
		}

		
		url = MessageFormat.format(PRICE_RULE_FORMAT, clientHost, priceRuleId);
		request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		response = client.newCall(request).execute();
		
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not fetch price rule: " + priceRuleId + ", " + discountCode);
		}
		
		String responseBody = response.body().string();

		ShopifyPriceRule wrapper = mapper.readValue(responseBody, ShopifyPriceRule.class);		
		return wrapper;
	}

	public ShopifyPriceRule updatePriceRule(ShopifyPriceRule priceRule) throws IOException {
		String url = MessageFormat.format(PRICE_RULE_FORMAT, clientHost, priceRule.getPriceRule().getId());
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(priceRule);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.put(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not update price rule: " + responseBody);
		}
		ShopifyPriceRule priceRuleResponse = mapper.readValue(responseBody, ShopifyPriceRule.class);
		return priceRuleResponse;
		
	}
	
	public static class CollectionsWrapper {
		@JsonProperty("smart_collections")
		@JsonAlias("custom_collections")
		public List<ShopifyCollection> collections;
	}
	
	public List<ShopifyCollection> getShopifySmartCollections() throws IOException {
		String url = MessageFormat.format(SHOPIFY_SMART_COLLECTIONS, clientHost);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		CollectionsWrapper wrapper = mapper.readValue(responseBodyString, CollectionsWrapper.class);
		return wrapper.collections;
	}
	
	public List<ShopifyCollection> getShopifyCustomCollections() throws IOException {
		String url = MessageFormat.format(SHOPIFY_CUSTOM_COLLECTIONS, clientHost);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		CollectionsWrapper wrapper = mapper.readValue(responseBodyString, CollectionsWrapper.class);
		return wrapper.collections;
	}
	

	
	public List<ShopifyProduct> getShopifyCollectionProducts(String id) throws IOException {
		String url = MessageFormat.format(SHOPIFY_COLLECTION_PRODUCTS, clientHost, id);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		ProductsWrapper wrapper = mapper.readValue(responseBodyString, ProductsWrapper.class);
		List<ShopifyProduct> list = new ArrayList<>(wrapper.products);
		
		String nextUrl = getNextPageLink(response);
		
		while (nextUrl != null) {
			request = new Request.Builder()
				      .url(nextUrl)
				      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				      .build();
			response = client.newCall(request).execute();
			responseBodyString = response.body().string();
			wrapper = mapper.readValue(responseBodyString, ProductsWrapper.class);
			list.addAll(wrapper.products);
			
			nextUrl = getNextPageLink(response);
		}
		
		
		return list;
	}
	
	
	
	public void updateShopifySmartCollectionOrder(String id, String sortingType, List<String> productIds) throws IOException {
		if (sortingType != null && !"manual".equals(sortingType) && !CollectionUtils.isEmpty(productIds)) {
			throw new RuntimeException("Incorrect sorting");
		}
		
		String url = MessageFormat.format(SHOPIFY_SMART_COLLECTION_ORDER, clientHost, id);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		if (StringUtils.isNotBlank(sortingType)) {
			params.add("sort_order", sortingType);
		}
		
		if(!CollectionUtils.isEmpty(productIds)) {
			params.addAll("products[]", productIds);
		}
		
		
		
		Request request = new Request.Builder()
				.url(buildUri(url, params))
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.put(RequestBody.create(null, new byte[] {}))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not update sorting in smart collection: " + responseBody);
		}
		return;
	}
	
	public void updateShopifyCustomCollection(String id, ShopifyCollectionCustomUpdate customCollectionUpdate) throws IOException {
		String url = MessageFormat.format(SHOPIFY_CUSTOM_COLLECTION, clientHost, id);
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(customCollectionUpdate);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.put(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not update custom collection: " + responseBody);
		}
		return;
	}
	
	public static class CollectsWrapper {
		@JsonProperty("collects")
		public List<ShopifyCollect> collects;
	}
	
	public List<ShopifyCollect> getShopifyCollects(String collectionId) throws IOException {
		
		String url = MessageFormat.format(SHOPIFY_COLLECTS, clientHost);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("limit", "250");
		if (StringUtils.isNotBlank(collectionId)) {
			params.add("collection_id", collectionId);
		}
		
		
		Request request = new Request.Builder()
			      .url(buildUri(url, params))
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		CollectsWrapper wrapper = mapper.readValue(responseBodyString, CollectsWrapper.class);
		List<ShopifyCollect> list = new ArrayList<ShopifyCollect>(wrapper.collects);
	
		String nextUrl = getNextPageLink(response);
		
		while (nextUrl != null) {
			request = new Request.Builder()
				      .url(nextUrl)
				      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				      .build();
			response = client.newCall(request).execute();
			responseBodyString = response.body().string();
			wrapper = mapper.readValue(responseBodyString, CollectsWrapper.class);
			list.addAll(wrapper.collects);
			
			nextUrl = getNextPageLink(response);
		}
		
		
		return list;
	}
	
	public static class VariantWrapper {
		@JsonProperty("variant")
		public ShopifyProductVariant variant;
	}
	
	public ShopifyProductVariant getShopifyVariant(String id) throws IOException {
		String url = MessageFormat.format(SHOPIFY_VARIANT, clientHost, id);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		if (StringUtils.isBlank(responseBodyString)) {
			return null;
		}
		
		VariantWrapper wrapper = mapper.readValue(responseBodyString, VariantWrapper.class);
		if (wrapper == null) {
			return null;
		}
		return wrapper.variant;
	}
	
	public static class ProductWrapper {
		@JsonProperty("variant")
		public ShopifyProduct variant;
	}
	
	public ShopifyProduct getShopifyProduct(String id) throws IOException {
		String url = MessageFormat.format(SHOPIFY_PRODUCT, clientHost, id);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		if (StringUtils.isBlank(responseBodyString)) {
			return null;
		}
		
		ProductWrapper wrapper = mapper.readValue(responseBodyString, ProductWrapper.class);
		if (wrapper == null) {
			return null;
		}
		return wrapper.variant;
	}
	
	
	private String getNextPageLink(Response response) {
		String headerLink = response.header("link");
		if (headerLink == null) {
			return null;
		}
		Map<String, String> links = Stream.of(headerLink.split(",")).collect(Collectors.toMap(
				h -> h.split(";")[1].trim(),
				h -> h.split(";")[0].replace('<',' ').replace('>', ' ').trim()));
		String nextUrl = links.get("rel=\"next\"");
		return nextUrl;
	}
	
	public void adjustInventory(ShopifyInventoryAdjustment inventoryAdjustment) throws IOException {
		String url = MessageFormat.format(SHOPIFY_ADJUST_INVENTORY, clientHost);
		ObjectMapper mapper = new ObjectMapper();
		String requestBody = mapper.writeValueAsString(inventoryAdjustment);
		Request request = new Request.Builder()
				.url(url)
				.header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
				.post(RequestBody.create(MediaType.get("application/json"), requestBody))
				.build();
		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Could not update inventory adjustment: " + responseBody);
		}
		return;
		
	}
	
	public static class LocationsWrapper {
		public List<ShopifyLocation> locations;
	}
	
	public List<ShopifyLocation> getLocations() throws Exception {
		String url = MessageFormat.format(LOCATIONS_FORMAT, clientHost);
		Request request = new Request.Builder()
			      .url(url)
			      .header(HttpHeaders.AUTHORIZATION, Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		LocationsWrapper wrapper = mapper.readValue(responseBodyString, LocationsWrapper.class);
		return wrapper.locations;
	}
	
}
