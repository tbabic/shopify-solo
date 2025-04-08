package org.bytepoet.shopifysolo.services;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.CsvBuilder;
import org.bytepoet.shopifysolo.manager.models.FileData;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BoxNowService {
	
	@Value("${boxnow.location}")
	private String location;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;

	
	public FileData generateAddressBookCsv(List<Order> orders) throws Exception {
		
		List<String> shopifyOrderIds = orders.stream().map(o -> o.getShopifyOrderId()).collect(Collectors.toList());
		List<ShopifyOrder> shopifyOrders = shopifyApiClient.getOrders(shopifyOrderIds, null, null, null, null);
		validate(shopifyOrders);
		Map<String, ShopifyOrder> map = shopifyOrders.stream().collect(Collectors.toMap(o -> o.getId(), o -> o));
		
		byte [] bytes = new CsvBuilder<Order>()
				.setEncoding("windows-1250")
				.setDataObjects(orders)
				
				.addHeaderAndField("from_location", o -> location)
				.addHeaderAndField("destination_location", o -> map.get(o.getShopifyOrderId()).getNoteAttribute("boxNowlockerId"))
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
	
	
	
}
