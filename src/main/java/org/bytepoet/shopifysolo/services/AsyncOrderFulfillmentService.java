package org.bytepoet.shopifysolo.services;

import java.util.List;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncOrderFulfillmentService {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderFulfillmentService orderFulfillmentService;
	
	@Async
	public void fullfillOrders() throws Exception {
		List<Order> orders = orderRepository.getByStatus(OrderStatus.IN_POST);
		for (Order order : orders) {
			orderFulfillmentService.fulfillOrder(order);
		}
	}
}
