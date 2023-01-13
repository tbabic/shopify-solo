package org.bytepoet.shopifysolo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.repositories.ProductPartDistributionRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductPartRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductRepository;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryProductService {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductPartRepository partRepository;
	
	@Autowired
	private ProductPartDistributionRepository distributionRepository;
	

	public void processNewOrder(Order order, ShopifyOrder shopifyOrder) {
		List<String> ids = shopifyOrder.getLineItems().stream().map(li -> li.getVariantId()).collect(Collectors.toList());
		
		List<Product> products = productRepository.findAndFetchByWebshopIds(ids);
		
		
	}
}
