package org.bytepoet.shopifysolo.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.CsvBuilder;
import org.bytepoet.shopifysolo.manager.models.FileData;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BoxNowService {
	
	@Value("${boxnow.location}")
	private String location;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	private static BoxNowLocationData locationData = null;
	
	private static Map<String, BoxNowLocation> locationsMap = new TreeMap<String, BoxNowLocation>();
	
	public static class BoxNowLocationData {
		@JsonProperty
		private List<BoxNowLocation> data;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class BoxNowLocation
	{
		@JsonProperty
		private String title;
		
		@JsonProperty
		private String id;
		
		@JsonProperty
		private String name;
	}

	
	public FileData generateAddressBookCsv(List<Order> orders) throws Exception {
		
		if (orders.isEmpty())
		{
			throw new RuntimeException("No orders selected");
		}
		List<String> shopifyOrderIds = orders.stream().map(o -> o.getShopifyOrderId()).collect(Collectors.toList());
		List<ShopifyOrder> shopifyOrders = shopifyApiClient.getOrders(shopifyOrderIds, null, null, null, null);
		//validate(shopifyOrders);
		Map<String, ShopifyOrder> map = shopifyOrders.stream().collect(Collectors.toMap(o -> o.getId(), o -> o));
		CsvBuilder<Order> builder = new CsvBuilder<>();
		byte [] bytes = new CsvBuilder<Order>()
				.setEncoding("UTF-8")
				.setDataObjects(orders)
				
				.addHeaderAndField("from_location", o -> location)
				.addHeaderAndField("destination_location", o -> this.getLocationId(map.get(o.getShopifyOrderId()).getShippingTitle()))
				.addHeaderAndField("customer_phone_number", o -> o.getShippingInfo().getPhoneNumber())
				.addHeaderAndField("customer", o -> o.getContact())
				.addHeaderAndField("customer_full_name", o -> o.getShippingInfo().getFullName())
				.build();

		
		String base64Value = Base64.encodeBase64String(bytes);
		
		return new FileData("adresnice.csv", base64Value);
	}
	
	
	private void validate(List<ShopifyOrder> orders) {
		boolean invalid = false;
		String message = "INVALID:";
		
		for (ShopifyOrder order: orders) {
			
			String lockerId = order.getNoteAttribute("boxNowlockerId");
			if (StringUtils.isBlank(lockerId)) {
				invalid = true;
				message += "\n " + order.getNumber() + " Missing box locker;";
			}
		}
		
		if (invalid) {
			throw new RuntimeException(message);
		}
	}
	
	
	private void loadFile() {
		
		
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			BoxNowService.locationData = mapper.readValue(new ClassPathResource("apms_hr-HR.json").getURL(), BoxNowLocationData.class);
			
			for(BoxNowLocation location : BoxNowService.locationData.data) {
				String key = location.title.trim() + ", " + location.name.trim();
				key = key.replaceAll(" ,", ",");
				if(locationsMap.containsKey(key))
		        {
		        	throw new RuntimeException("box now location key already exists: " + key);
		        }
				
		        locationsMap.put(key, location);
		        
		    }
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		

		
	}
	

	
	private String getLocationId(String shippingString) {
		if (locationsMap.isEmpty()) {
			loadFile();
		}
		
		String key = shippingString.replaceFirst("BOX NOW – ", "").replaceAll(" ,", ",").trim();
		if (!locationsMap.containsKey(key))
		{
			throw new RuntimeException("No locker for: " + shippingString);
		}
		return locationsMap.get(key).id;
		
	}
	
	
	
}
