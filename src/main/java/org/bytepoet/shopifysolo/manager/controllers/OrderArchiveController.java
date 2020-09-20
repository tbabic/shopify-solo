package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderArchive;
import org.bytepoet.shopifysolo.manager.repositories.OrderArchiveRepository;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/orders-archive")
public class OrderArchiveController {
	
	private static final String DATE_FORMAT = "dd.MM.yyyy:HH:mm:ss_z";
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderArchiveRepository orderArchiveRepository;

	@RequestMapping(path = "/add", method=RequestMethod.POST)
	@Transactional
	public void addToArchive(@DateTimeFormat(pattern = DATE_FORMAT) @RequestParam("start") Date start, 
			@DateTimeFormat(pattern = DATE_FORMAT) @RequestParam("end") Date end) {
		OrderArchive archive = orderArchiveRepository.findAll().stream().findFirst().orElse(new OrderArchive());
		List<Order> orders = orderRepository.getByCreationDateBetween(start, end);
		orders.stream().forEach(order -> archive.addOrder(order));
		archive.updateData();
		orderArchiveRepository.saveAndFlush(archive);
		orderRepository.deleteAll(orders);
		return;
	}
	
	@RequestMapping(path = "/add-orders", method=RequestMethod.POST)
	@Transactional
	public void addToArchive(@RequestBody Set<Long> orderIds) {
		OrderArchive archive = orderArchiveRepository.findAll().stream().findFirst().orElse(new OrderArchive());
		List<Order> orders = orderRepository.findAllById(orderIds);
		orders.stream().forEach(order -> archive.addOrder(order));
		
		orderArchiveRepository.save(archive);
		orderRepository.deleteAll(orders);
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public OrderArchive viewArchive() {
		return orderArchiveRepository.findAll().stream().findFirst().orElse(new OrderArchive());
	}
	
	@RequestMapping(path = "/restore", method=RequestMethod.POST)
	@Transactional
	public List<Order> restoreFromArchive(@RequestBody Set<Long> orderIds) {
		OrderArchive archive = orderArchiveRepository.findAll().stream().findFirst().orElse(new OrderArchive());
		List<Order> orders = archive.getOrders().stream()
			.filter(order -> orderIds.contains(order.getId()))
			.collect(Collectors.toList());
		
		orderRepository.saveAll(orders);
		archive.getOrders().removeAll(orders);
		orderArchiveRepository.save(archive);
		return orders;
	}
	
	
}
