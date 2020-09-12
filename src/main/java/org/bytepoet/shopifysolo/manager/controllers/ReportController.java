package org.bytepoet.shopifysolo.manager.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.models.OrderType;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.ShippingSearchStatus;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/manager/reports")
public class ReportController {

	private static final String DATE_FORMAT = "dd.MM.yyyy:HH:mm:ss_z";
	
	@Autowired
	private OrderRepository orderRepository;
	
	public static class Report {
		@JsonProperty
		int orderCount;
		@JsonProperty
		double orderSum;
	}
	
	
	@RequestMapping(method=RequestMethod.GET)
	public Report addToArchive(@DateTimeFormat(pattern = DATE_FORMAT) @RequestParam("start") Date start, 
			@DateTimeFormat(pattern = DATE_FORMAT) @RequestParam("end") Date end) {

		List<PaymentOrder> order = orderRepository.getByPamentDateBetween(start, end);
		
		Report report = new Report();
		report.orderCount = order.size();
		report.orderSum = order.stream().collect(Collectors.summingDouble(p -> p.getTotalPrice())).doubleValue();
		return report;
		
	}

}
