package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Currency;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.manager.repositories.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
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
	
	@Autowired
	private RefundRepository refundRepository;
	
	public static class Report {
		@JsonProperty
		int orderCount;
		@JsonProperty
		double orderSum;
		
		@JsonProperty
		int refundCount;
		@JsonProperty
		double refundSum;
		
		@JsonProperty
		double totalSum;
	}
	
	
	@RequestMapping(method=RequestMethod.GET)
	public Report getReports(@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam("start") Date start, 
			@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam("end") Date end) {

		List<PaymentOrder> order = orderRepository.getByPamentDateBetween(start, end);
		
		List<Refund> refunds = refundRepository.getByInvoiceDateBetween(start, end);
		
		Report report = new Report();
		report.orderCount = order.size();
		report.orderSum = order.stream().collect(Collectors.summingDouble(p -> p.getTotalPrice(Currency.EUR))).doubleValue();
		
		report.refundCount = refunds.size();
		report.refundSum = refunds.stream().collect(Collectors.summingDouble(r -> r.getTotalPrice(Currency.EUR))).doubleValue();
		
		report.totalSum = report.orderSum - report.refundSum;
		return report;
		
	}

}
