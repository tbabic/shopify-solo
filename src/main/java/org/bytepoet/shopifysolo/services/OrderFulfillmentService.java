package org.bytepoet.shopifysolo.services;

import java.text.MessageFormat;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyFulfillment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class OrderFulfillmentService {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderFulfillmentService.class);

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@Autowired
	private FulfillmentMaillingService fulfillmentMaillingService;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void fulfillOrder(Order order) throws Exception {
		if(StringUtils.isBlank(order.getTrackingNumber())) {
			return;
		}
		if (order.getStatus()!= OrderStatus.IN_PROCESS) {
			logger.info("Order not in process: " + order.getId());
		}
		order.fulfill(order.getTrackingNumber());
		order = orderRepository.save(order);
		syncOrder(order, true);
		
	}
	
	private boolean syncOrder(Order order, boolean sendNotification) throws Exception {
		List<ShopifyFulfillment> fulfillments = shopifyApiClient.getFulfillments(order.getShopifyOrderId());
		sendNotification = sendNotification & !order.isPersonalTakeover();
		logger.info(MessageFormat.format("notification id: {0}, shopify: {1}, {2}", order.getShopifyOrderNumber(), order.getShopifyOrderId(), sendNotification));
		if (CollectionUtils.isEmpty(fulfillments)) {
			shopifyApiClient.fulfillOrder(order.getShopifyOrderId(), order.getTrackingNumber(), order.getShippingType(), sendNotification);
			return true;
		} else {
			shopifyApiClient.updateFulfillment(order.getShopifyOrderId(), fulfillments.get(0).id, order.getTrackingNumber(), order.getShippingType(), sendNotification);
		}
		return false;
	}
	
}
