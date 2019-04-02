package org.bytepoet.shopifysolo.controllers;

import org.bytepoet.shopifysolo.mappers.ShopifyToSoloMapper;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RestController
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private ShopifyToSoloMapper mapper;
	
	@PostMapping
	public void orders(@RequestBody ShopifyOrder order) {
		logger.debug(order.toString());
		soloApiClient.createReceipt(mapper.map(order));
	}
}
