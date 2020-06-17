package org.bytepoet.shopifysolo.controllers;

import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tenders/{token}/payment-info")
public class PaymentInfoController {

	
	@Autowired
	private OrderRepository orderRepository;
	
	@RequestMapping
	public Object getPaymentInfo(@PathVariable("token") String shopifyId) {
		return null;
	}
	
}
