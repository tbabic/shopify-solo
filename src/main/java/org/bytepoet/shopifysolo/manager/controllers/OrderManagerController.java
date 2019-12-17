package org.bytepoet.shopifysolo.manager.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.bytepoet.shopifysolo.manager.models.GiveawayOrder;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderType;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.mappers.OrderToSoloInvoiceMapper;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
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
	private SoloApiClient soloApiClient;
	
	@Autowired
	private OrderToSoloInvoiceMapper orderToSoloInvoiceMapper;
	
	@Autowired
	private OrderRepository orderRepository;
	
	

	
	@RequestMapping(method=RequestMethod.GET)
	public Page<Order> getOrders(
			@RequestParam(name="open", required=false) Boolean isOpen,
			@RequestParam(name="paid", required=false) Boolean isPaid,
			@RequestParam(name="personalTakeover", required=false) Boolean isPersonalTakeover,
			@RequestParam(name="type", required=false) OrderType type,
			@RequestParam(name="page", required=false, defaultValue = "0") int page,
			@RequestParam(name="size", required=false, defaultValue = "20") int size,
			@RequestParam(name="sortBy", required=false) String sortBy,
			@RequestParam(name="sortDirection", required=false) Direction direction) throws Exception {
		
		Specification<Order> spec = new Specification<Order>() {
			
			private static final long serialVersionUID = -7241789667400058644L;

			@Override
			public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				if((query.getResultType() != Long.class) && (query.getResultType() != long.class)) {
					root.fetch("items");
				}
				
				Root<?> actualRoot = root;
				List<Predicate> predicates = new ArrayList<>();
				if((type != null && type == OrderType.PAYMENT) || (type == null && isPaid != null)) {
					actualRoot = criteriaBuilder.treat(root, PaymentOrder.class);
					if (isPaid != null) {
						predicates.add(criteriaBuilder.equal(actualRoot.get("isPaid"), isPaid.booleanValue()));
					}
				} else if (type == OrderType.GIVEAWAY) {
					actualRoot = criteriaBuilder.treat(root, GiveawayOrder.class);
				}				
				if(isOpen != null) {
					predicates.add(criteriaBuilder.equal(actualRoot.get("isFulfilled"), !isOpen.booleanValue()));
				}
				if(isPersonalTakeover != null) {
					predicates.add(criteriaBuilder.equal(actualRoot.get("isPersonalTakeover"), isPersonalTakeover.booleanValue()));
				}
				
				return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
			}
		};
		Pageable pageable;
		if (sortBy == null) {
			pageable = PageRequest.of(page, size);
		} else if (direction == null) {
			pageable = PageRequest.of(page, size, Sort.by(sortBy));
		} else {
			pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
		}
		return orderRepository.findAll(spec, pageable);
	}
	
	
	@RequestMapping(method=RequestMethod.POST)
	public Order save(Order order) {
		orderRepository.save(order);
		return order;
	}
	
	@RequestMapping(path="/{id}/processPayment", method=RequestMethod.POST)
	public Order processPayment(@PathVariable("id") Long orderId, @RequestParam(name="paymentDate", required=false) Date paymentDate) {
		Order order = orderRepository.getOne(orderId);
		if (!(order instanceof PaymentOrder)) {
			throw new RuntimeException("Order with id: " + orderId + " is not payment order");
		}
		PaymentOrder paymentOrder = (PaymentOrder) order;
		SoloInvoice soloInvoice = soloApiClient.createInvoice(orderToSoloInvoiceMapper.map(paymentOrder));
		paymentOrder.updateFromSoloInvoice(soloInvoice, paymentDate);
		return paymentOrder;
	}
	
}
