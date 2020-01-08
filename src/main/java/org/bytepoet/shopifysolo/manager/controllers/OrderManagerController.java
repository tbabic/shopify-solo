package org.bytepoet.shopifysolo.manager.controllers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.bytepoet.shopifysolo.manager.models.GiveawayOrder;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.models.OrderType;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.mappers.OrderToSoloInvoiceMapper;
import org.bytepoet.shopifysolo.services.FulfillmentMaillingService;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyTransaction;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/orders")
public class OrderManagerController {
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private OrderToSoloInvoiceMapper orderToSoloInvoiceMapper;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private FulfillmentMaillingService fulfillmentMaillingService;
	
	@Value("${email.subject}")
	private String subject;
	
	@Value("${email.body}")
	private String body;
	
	@Value("${email.always-bcc:}")
	private String alwaysBcc;
	
	
	
	
	@RequestMapping(method=RequestMethod.GET)
	public Page<Order> getOrders(
			@RequestParam(name="open", required=false) Boolean isOpen,
			@RequestParam(name="paid", required=false) Boolean isPaid,
			@RequestParam(name="personalTakeover", required=false) Boolean isPersonalTakeover,
			@RequestParam(name="type", required=false) OrderType type,
			@RequestParam(name="status", required=false) List<OrderStatus> statusList,
			@RequestParam(name="page", required=false, defaultValue = "0") int page,
			@RequestParam(name="size", required=false, defaultValue = "20") int size,
			@RequestParam(name="sortBy", required=false, defaultValue ="id") String sortBy,
			@RequestParam(name="sortDirection", required=false, defaultValue ="ASC") Direction direction) throws Exception {
		
		Specification<Order> spec = new Specification<Order>() {
			
			private static final long serialVersionUID = -7241789667400058644L;

			@Override
			public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				if((query.getResultType() != Long.class) && (query.getResultType() != long.class)) {
					root.fetch("items", JoinType.LEFT);
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
				if (statusList != null && !statusList.isEmpty()) {
					predicates.add(actualRoot.get("status").in(statusList));
				}
				if(isPersonalTakeover != null) {
					predicates.add(criteriaBuilder.equal(actualRoot.get("personalTakeover"), isPersonalTakeover.booleanValue()));
				}
				
				return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
			}
		};
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
		Page<Order> orders = orderRepository.findAll(spec, pageable);
		return orders;
	}
	
	
	@RequestMapping(method=RequestMethod.POST)
	public Order save(@RequestBody Order order) {
		Order savedOrder = orderRepository.save(order);
		return savedOrder;
	}
	
	@RequestMapping(path="/{id}", method=RequestMethod.GET)
	public Order getOrder(@PathVariable("id") Long orderId) {
		return orderRepository.findById(orderId).get();
	}
	
	@RequestMapping(path="/{id}/process-fulfillment", method=RequestMethod.POST)
	public void fullfilment(@PathVariable("id") Long orderId, 
			@RequestParam(name="trackingNumber", required=false) String trackingNumber, 
			@RequestParam(name="sendNotification", required=false, defaultValue = "false") boolean sendNotification) {
		Order order = orderRepository.getOne(orderId);
		order.fulfill(trackingNumber);
		if (sendNotification) {
			fulfillmentMaillingService.sendFulfillmentEmail(order.getContact(), trackingNumber);
		}
		orderRepository.save(order);
		
	}
	
	@RequestMapping(path="/{id}/process-payment", method=RequestMethod.POST)
	public void processPayment(@PathVariable("id") Long orderId, @RequestParam(name="paymentDate", required=false) Date paymentDate) throws Exception {
		PaymentOrder paymentOrder = orderRepository.getPaymentOrderById(orderId).get();
		if (paymentOrder.isPaid()) {
			throw new RuntimeException("Order is already paid for"); 
		}
		
		List<ShopifyTransaction> transactions  = shopifyApiClient.getTransactions(paymentOrder.getShopifyOrderId());
		if (transactions.size() != 1) {
			throw new RuntimeException(MessageFormat.format("Order has {0} tranasctions but must have 1", transactions.size())); 
		}
		ShopifyTransaction transaction = transactions.get(0);
		if (!"pending".equalsIgnoreCase(transaction.getStatus())) {
			throw new RuntimeException(MessageFormat.format("Transaction status is {0} but must be 'pending'", transaction.getStatus())); 
		}
		
		SoloInvoice soloInvoice = soloApiClient.createInvoice(orderToSoloInvoiceMapper.map(paymentOrder));
		paymentOrder.updateFromSoloInvoice(soloInvoice, paymentDate);
		
		orderRepository.save(paymentOrder);
		soloMaillingService.sendEmailWithPdf(paymentOrder.getEmail(), alwaysBcc, soloInvoice.getPdfUrl(), subject, body);
		paymentOrder.setReceiptSent(true);
		orderRepository.save(paymentOrder);
		
		shopifyApiClient.createTransaction(transaction.createNewTransaction(), paymentOrder.getShopifyOrderId());
	}
	
}
