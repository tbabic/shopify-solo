package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.epk.model.EpkBook;
import org.bytepoet.shopifysolo.manager.models.FileData;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.GlsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/manager/gls")
@RestController
public class GlsController {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private GlsService glsService;
	
	@RequestMapping(path="/createAddressBook", method = RequestMethod.POST)
	public FileData createAddressSlipCvs(@RequestBody List<Long> orderIds) {
		List<Order> orders = orderRepository.findAllById(orderIds);
		
		
		return glsService.generateAddressBookCsv(orders);
		
	}
}
