package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.epk.EpkService;
import org.bytepoet.shopifysolo.epk.model.EpkBook;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/manager/epk")
@RestController
public class EpkController {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private EpkService epkService;

	@RequestMapping(method = RequestMethod.POST)
	public EpkBook createEpk(@RequestBody List<Long> orderIds) {
		List<Order> orders = orderRepository.findAllById(orderIds);
		orders.stream().forEach(order -> {
			if (StringUtils.isBlank(order.getTrackingNumber())) {
				throw new RuntimeException("Narudzba " + order.getId() + " nema tracking broj");
			}
		});
		List<Order> sorted = orders.stream().sorted(Comparator.comparing(Order::getTrackingNumber)).collect(Collectors.toList());
		return epkService.generateEpk(sorted);
		
	}
	
}
