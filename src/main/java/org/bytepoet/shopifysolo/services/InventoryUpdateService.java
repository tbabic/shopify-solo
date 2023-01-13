package org.bytepoet.shopifysolo.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.InventoryJobStatus;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.models.ProductPart;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductPartRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductRepository;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryUpdateService {
	
	@Autowired
	private ProductPartRepository productPartRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@Autowired
	private TransactionalService transactionalService;

	public void updateInventory(Order order, ShopifyOrder shopifyOrder) {
		processExistingJob(order, shopifyOrder);
	}
	
	

	public void processJobs() throws Exception {
		List<Order> orders = orderRepository.findByInventoryJob(InventoryJobStatus.PENDING);
		
		for (Order order : orders) {
			ShopifyOrder shopifyOrder = shopifyApiClient.getOrder(order.getShopifyOrderId());
			processExistingJob(order, shopifyOrder );
		}
		
		
		
	}
	
	
	private void processExistingJob(Order order, ShopifyOrder shopifyOrder) {
		synchronized(InventoryUpdateService.class) {
			InventoryJobStatus status =orderRepository.getJobStatus(order.getId());
			if (status != InventoryJobStatus.PENDING) {
				return;
			}
			
			
			order.setInventoryJob(InventoryJobStatus.IN_PROCESS);
			orderRepository.updateJobStatus(order.getInventoryJob(), order.getId());
			
			Map<String, ShopifyLineItem> lineItemsMap = shopifyOrder.getLineItems().stream().collect(Collectors.toMap(item -> item.getVariantId(), item -> item));
			List<String> webshopIds = lineItemsMap.keySet().stream().collect(Collectors.toList());
			List<Product> products = productRepository.findAndFetchByWebshopIds(webshopIds);
			products.forEach(product -> {
				ShopifyLineItem lineItem = lineItemsMap.get(product.getWebshopId());
				product.reduceAvailability(lineItem.getQuantity());
			});
			
			List<ProductPart> parts = products.stream().flatMap(p -> p.getParts().stream()).collect(Collectors.toList());
			productPartRepository.saveAll(parts);
			order.setInventoryJob(InventoryJobStatus.COMPLETED);
			orderRepository.updateJobStatus(order.getInventoryJob(), order.getId());
		}
	}
	
}
