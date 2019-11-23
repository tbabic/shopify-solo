package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.manager.repositories.Sorting;
import org.bytepoet.shopifysolo.mappers.OrderToSoloInvoiceMapper;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/orders")
public class OrderManagerController {
	
	@Value("${shopify.api.host}")
	private String clientHost;
	
	@Value("${shopify.api.key}")
	private String clientUsername;
	
	@Value("${shopify.api.password}")
	private String clientPassword;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private OrderToSoloInvoiceMapper orderToSoloInvoiceMapper;
	
	

	
	@RequestMapping(method=RequestMethod.GET)
	public List<Order> getOrders(
			@RequestParam(name="open", required=false) Boolean isOpen,
			@RequestParam(name="paid", required=false) Boolean isPaid, 
			@RequestParam(name="sortBy", required=false) String sortBy,
			@RequestParam(name="sortDirection", required=false) Sorting.Direction sortDirection) throws Exception {
		
		Comparator<Order> sorter = getSorter(sortBy, sortDirection);
		List<Order> orders;
		if (sorter == null) {
			orders = orderRepository.getAllWhere(o -> matchOrder(o, isOpen, isPaid));
		} else {
			orders = orderRepository.getAllOrderedWhere(sorter, o -> matchOrder(o, isOpen, isPaid));
		}
		return orders;
	}
	
	
	@RequestMapping(method=RequestMethod.POST)
	public Order save(Order order) {
		return orderRepository.save(order);
	}
	
	@RequestMapping(path="/{id}/processPayment", method=RequestMethod.POST)
	public Order processPayment(@PathVariable("id") Long orderId, @RequestParam(name="paymentDate", required=false) Date paymentDate) {
		Order order = orderRepository.getById(orderId);
		if (!(order instanceof PaymentOrder)) {
			throw new RuntimeException("Order with id: " + orderId + " is not payment order");
		}
		PaymentOrder paymentOrder = (PaymentOrder) order;
		SoloInvoice soloInvoice = soloApiClient.createInvoice(orderToSoloInvoiceMapper.map(paymentOrder));
		paymentOrder.updateFromSoloInvoice(soloInvoice, paymentDate);
		return paymentOrder;
	}
	
	
	private Comparator<Order> getSorter(String sortBy, Sorting.Direction sortDirection) {
		if (StringUtils.isBlank(sortBy)) {
			return null;
		}
		if (sortDirection == null) {
			sortDirection = Sorting.Direction.ASC;
		}
		return Sorting.<Order>orderBy( order ->  {
			if(sortBy.equals("id")) {
				return order.getId();
			}
			if (sortBy.equals("creationDate")) {
				return order.getCreationDate();
			}
			if (sortBy.equals("sendingDate")) {
				return order.getSendingDate();
			}
			return 0;
		}, sortDirection);
	}
	
	private boolean matchOrder(Order order, Boolean isOpen, Boolean isPaid) {
		if(isOpen != null && order.isFulfilled() != isOpen.booleanValue()) {
			return false;
		}
		if(isPaid != null && ((PaymentOrder) order).isPaid() != isPaid.booleanValue()) {
			return false;
		}
		return true;
			
	}

	
}
