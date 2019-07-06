package org.bytepoet.shopifysolo.manager.controllers;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RestController
@RequestMapping("/manager/orders")
public class OrderManagerController {
	
	private static final String ENDPOINT_FORMAT = "https://{0}/admin/api/2019-04/orders.json";
	
	private static final String SHOPIFY_DATE_PATTERN = "";
	
	@Value("${shopify.api.host}")
	private String clientHost;
	
	@Value("${shopify.api.username}")
	private String clientUsername;
	
	@Value("${shopify.api.password}")
	private String clientPassword;
	
	@GetMapping
	public List<ShopifyOrder> getOrders(
			@RequestParam(name="paid", required=false) Boolean isPaid, 
			@RequestParam(name="open", required=false) Boolean isOpen, 
			@RequestParam(name="after", required=false) Date afterDate, 
			@RequestParam(name="before", required=false) Date beforeDate) throws Exception {
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
			      .header("Authrorization", Credentials.basic(clientUsername, clientPassword))
			      .build();
		Response response = client.newCall(request).execute();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<ShopifyOrder> list = mapper.readValue(response.body().byteStream(), new TypeReference<List<ShopifyOrder>>(){});
		return list;
	}
	
	private String buildUri(String url, MultiValueMap<String, String> params) {
	    UriComponents uriComponents = UriComponentsBuilder.newInstance()
	            .queryParams(params).build();

	   return url+uriComponents.toString();
	}
	
}
