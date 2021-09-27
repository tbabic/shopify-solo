package org.bytepoet.shopifysolo.manager.controllers;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.controllers.OrderController;
import org.bytepoet.shopifysolo.controllers.TenderController;
import org.bytepoet.shopifysolo.manager.models.GiveawayOrder;
import org.bytepoet.shopifysolo.manager.models.Invoice;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.models.OrderType;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.PaymentType;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.manager.models.RefundInvoice;
import org.bytepoet.shopifysolo.manager.models.ShippingSearchStatus;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.manager.repositories.RefundRepository;
import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.services.AsyncOrderFulfillmentService;
import org.bytepoet.shopifysolo.services.FulfillmentMaillingService;
import org.bytepoet.shopifysolo.services.InvoiceService;
import org.bytepoet.shopifysolo.services.MailService;
import org.bytepoet.shopifysolo.services.OrderFulfillmentService;
import org.bytepoet.shopifysolo.services.PdfInvoiceService;
import org.bytepoet.shopifysolo.services.PdfRefundService;
import org.bytepoet.shopifysolo.services.RefundService;
import org.bytepoet.shopifysolo.services.MailService.MailAttachment;
import org.bytepoet.shopifysolo.services.MailService.MailReceipient;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateDraftOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateDraftOrder.Discount;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateDraftOrder.LineItem;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateDraftOrder.ShippingAddress;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyFulfillment;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/manager/orders")
public class OrderManagerController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderManagerController.class);
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private RefundService refundService;
	
	@Autowired
	private PdfInvoiceService pdfInvoiceService;
	
	@Autowired
	private PdfRefundService pdfRefundService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private RefundRepository refundRepository;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private FulfillmentMaillingService fulfillmentMaillingService;
	
	@Autowired
	private GatewayToPaymentTypeMapper paymentTypeMapper;
	
	@Value("${email.subject}")
	private String subject;
	
	@Value("${email.body}")
	private String body;
	
	@Value("${email.refund-body}")
	private String refundBody;
	
	@Value("${email.always-bcc:}")
	private String alwaysBcc;
	
	@Value("${error.email:}")
	private String errorEmail;
	
	@Value("${shopify.bank-deposit-gateway}")
	private List<String> bankDepositGateway;
	
	@Value("${solofy.tax-rate:}")
	private String taxRate;
	
	@Value("${soloapi.shipping-title}")
	private String shippingTitle;
	
	@Autowired
	private OrderFulfillmentService orderFulfillmentService;
	
	@Autowired
	private AsyncOrderFulfillmentService asyncOrderFulfillmentService;
	
	
	@RequestMapping(method=RequestMethod.GET)
	public Page<Order> getOrders(
			@RequestParam(name="open", required=false) Boolean isOpen,
			@RequestParam(name="paid", required=false) Boolean isPaid,
			@RequestParam(name="personalTakeover", required=false) Boolean isPersonalTakeover,
			@RequestParam(name="type", required=false) OrderType type,
			@RequestParam(name="status", required=false) List<OrderStatus> statusList,
			@RequestParam(name="shippingSearchStatus", required=false) ShippingSearchStatus shippingSearchStatus,
			@RequestParam(name="hasNote", required=false) Boolean hasNote,
			@RequestParam(name="search", required=false) String search,
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
				if(type == OrderType.PAYMENT) {
					actualRoot = criteriaBuilder.treat(root, PaymentOrder.class);
					if (isPaid != null) {
						predicates.add(criteriaBuilder.equal(actualRoot.get("isPaid"), isPaid.booleanValue()));
					}
				} else if (type == OrderType.GIVEAWAY) {
					actualRoot = criteriaBuilder.treat(root, GiveawayOrder.class);
				} else if (isPaid != null) {
					Predicate p1 = criteriaBuilder.equal(criteriaBuilder.treat(root, PaymentOrder.class).get("isPaid"), isPaid.booleanValue());
					Predicate p2 = criteriaBuilder.equal(criteriaBuilder.treat(root, GiveawayOrder.class).type(), GiveawayOrder.class);
					predicates.add(criteriaBuilder.or(p1, p2));
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
				if(shippingSearchStatus != null) {
					predicates.add(criteriaBuilder.equal(actualRoot.get("shippingSearchStatus"), shippingSearchStatus));
				}
				if(hasNote != null) {
					if (hasNote.booleanValue()) {
						predicates.add(criteriaBuilder.and(
								actualRoot.get("note").isNotNull(), 
								criteriaBuilder.notEqual(actualRoot.get("note"), "")) );
					} else {
						predicates.add(criteriaBuilder.or(
								actualRoot.get("note").isNull(), 
								criteriaBuilder.equal(actualRoot.get("note"), "")) );
					}
					
				}
				if (StringUtils.isNotBlank(search)) {
					List<Predicate> searchPredicates = new ArrayList<>();
					searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(actualRoot.get("note")), "%"+search.toLowerCase()+"%"));
					searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(actualRoot.get("contact")), "%"+search.toLowerCase()+"%"));
					searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(actualRoot.get("shippingInfo").get("fullName")), "%"+search.toLowerCase()+"%"));
					
					boolean paymentOrderIncluded = false;
					
					if(actualRoot == root) {
						paymentOrderIncluded = true;
					} else if(actualRoot.equals(criteriaBuilder.treat(root, PaymentOrder.class))) {
						paymentOrderIncluded = true;
					}
					
					if(paymentOrderIncluded) {
						Subquery<PaymentOrder> sq = query.subquery(PaymentOrder.class);
						Root<PaymentOrder> sqRoot = sq.from(PaymentOrder.class);
			
						sq.select(sqRoot.get("id")).where(criteriaBuilder.or(
								criteriaBuilder.like(sqRoot.get("tenderNumber"), "%"+search+"%"),
								criteriaBuilder.like(sqRoot.get("invoice").get("number"), "%"+search+"%"),
								criteriaBuilder.like(sqRoot.get("shopifyOrderNumber"), "%"+search+"%")));
						searchPredicates.add(criteriaBuilder.in(actualRoot.get("id")).value(sq));
			
					}
					predicates.add(criteriaBuilder.and(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0]))));

				}
				if(predicates.isEmpty()) {
					if (type == OrderType.GIVEAWAY) {
						predicates.add(criteriaBuilder.equal(root.type(), GiveawayOrder.class));
					} else if (type == OrderType.PAYMENT ){
						predicates.add(criteriaBuilder.equal(root.type(), PaymentOrder.class));
					} else {
						return null;
					}
				}
				return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
			}
		};
		if(direction == null) {
			direction = Direction.ASC;
		}
		if(sortBy == null) {
			sortBy = "id";
		}
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
		Page<Order> orders = orderRepository.findAll(spec, pageable);
		return orders;
	}
	
	@RequestMapping(path="/process-fulfillment-in-post", method=RequestMethod.POST)
	public void fullfillOrders() throws Exception {
		List<Order> orders = orderRepository.getByStatus(OrderStatus.IN_POST);
		for (Order order : orders) {
			orderFulfillmentService.fulfillOrder(order);
		}
	}
	
	@RequestMapping(path="/process-fulfillment-in-post-async", method=RequestMethod.POST)
	public void fullfillOrdersAsync() throws Exception {
		asyncOrderFulfillmentService.fullfillOrders();
	}
	
	
	@RequestMapping(path="/create-shopify-giveaway", method=RequestMethod.POST)
	public Order saveShopifyGiveaway(@RequestBody ShopifyCreateOrder shopifyCreateOrder, @RequestParam(name="giveawayPlatform", required=false) String platform) throws Exception {

		synchronized(OrderController.class) {
			ShopifyOrder shopifyOrder = shopifyApiClient.createOrder(shopifyCreateOrder);
			return orderRepository.saveAndFlush(new GiveawayOrder(shopifyOrder, platform));
		}
		
		
	}
	
	@RequestMapping(path="/create-shopify-order", method=RequestMethod.POST)
	public Order saveShopifyOrder(@RequestBody ShopifyCreateDraftOrder shopifyCreateOrder) throws Exception {

		synchronized(TenderController.class) {
			String draftId = shopifyApiClient.createDraftOrder(shopifyCreateOrder);
			String shopifyOrderId = shopifyApiClient.completeDraftOrder(draftId);
			ShopifyOrder shopifyOrder = shopifyApiClient.getOrder(shopifyOrderId);
			return orderRepository.saveAndFlush(new PaymentOrder(shopifyOrder, PaymentType.BANK_TRANSACTION, taxRate, shippingTitle));
		}
		
		
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public Order save(@RequestBody Order order) {
		if (order.getStatus()!= OrderStatus.IN_PROCESS) {
			logger.warn("Order not in process: " + order.getId());
		}
		if (order.getId() != null || order.getType() == OrderType.GIVEAWAY) {
			Order savedOrder = orderRepository.save(order);
			return savedOrder;
		}
		return null;
	}
	
	@RequestMapping(path="/{id}", method=RequestMethod.GET)
	public Order getOrder(@PathVariable("id") Long orderId) {
		return orderRepository.findById(orderId).get();
	}
	
	@RequestMapping(path="/{id}/process-fulfillment", method=RequestMethod.POST)
	public void fullfilment(@PathVariable("id") Long orderId, 
			@RequestParam(name="trackingNumber", required=false) String trackingNumber, 
			@RequestParam(name="sendNotification", required=false, defaultValue = "false") boolean sendNotification) throws Exception {
		Order order = orderRepository.getOne(orderId);
		order.fulfill(trackingNumber);
		order = orderRepository.save(order);
		if (order instanceof PaymentOrder) {
			syncOrder((PaymentOrder) order, sendNotification);
		}
	}
	
	@RequestMapping(path="/{id}/process-payment", method=RequestMethod.POST)
	public void processPayment(@PathVariable("id") Long orderId, 
			@RequestParam(name="paymentDate", required=false) Date paymentDate,
			@RequestParam(name="r1", required=false) boolean r1,
			@RequestParam(name="oib", required=false) String oib) throws Exception {
		PaymentOrder paymentOrder = orderRepository.getPaymentOrderById(orderId).get();
		if (paymentOrder.isPaid()) {
			throw new RuntimeException("Order is already paid for"); 
		}
		
		
		
		paymentOrder.applyTaxRate(taxRate);
		Invoice invoice = invoiceService.createInvoice(paymentOrder);
		paymentOrder.updateInvoice(invoice);
		orderRepository.save(paymentOrder);
		
		
		
		byte [] pdfInvoice = pdfInvoiceService.createInvoice(paymentOrder, r1, oib);
		
		sendEmail(paymentOrder.getEmail(), invoice.getNumber(), pdfInvoice);
		paymentOrder.setReceiptSent(true);
		orderRepository.save(paymentOrder);
		
		List<ShopifyTransaction> transactions  = shopifyApiClient.getTransactions(paymentOrder.getShopifyOrderId());
		if (transactions.size() != 1) {
			throw new RuntimeException(MessageFormat.format("Order has {0} tranasctions but must have 1", transactions.size())); 
		}
		ShopifyTransaction transaction = transactions.get(0);
		if (!"pending".equalsIgnoreCase(transaction.getStatus())) {
			throw new RuntimeException(MessageFormat.format("Transaction status is {0} but must be 'pending'", transaction.getStatus())); 
		}
		
		shopifyApiClient.createTransaction(transaction.createNewTransaction(), paymentOrder.getShopifyOrderId());
	}
	
	
	
	
	
	@RequestMapping(path="/{id}/reissue-invoice", method=RequestMethod.POST)
	public void reissueInvoice(@PathVariable("id") Long orderId, 
			@RequestParam(name="paymentDate", required=false) Date paymentDate,
			@RequestParam(name="r1", required=false) boolean r1,
			@RequestParam(name="oib", required=false) String oib) throws Exception {
		
		PaymentOrder paymentOrder = orderRepository.getPaymentOrderById(orderId).get();
		byte [] pdfInvoice = pdfInvoiceService.createInvoice(paymentOrder, r1, oib);
		
		sendEmail(paymentOrder.getEmail(), paymentOrder.getInvoiceNumber(), pdfInvoice);
		paymentOrder.setReceiptSent(true);
		orderRepository.save(paymentOrder);
		
		if (paymentOrder.getPaymentType() == PaymentType.CREDIT_CARD) {
			return;
		}
		
		List<ShopifyTransaction> transactions  = shopifyApiClient.getTransactions(paymentOrder.getShopifyOrderId());
		if (transactions.size() != 1) {
			throw new RuntimeException(MessageFormat.format("Order has {0} tranasctions but must have 1", transactions.size())); 
		}
		ShopifyTransaction transaction = transactions.get(0);
		if (!"pending".equalsIgnoreCase(transaction.getStatus())) {
			throw new RuntimeException(MessageFormat.format("Transaction status is {0} but must be 'pending'", transaction.getStatus())); 
		}
		
		shopifyApiClient.createTransaction(transaction.createNewTransaction(), paymentOrder.getShopifyOrderId());
		
	}
	
	public class PdfInvoiceContent {

		@JsonProperty
		private String fileName;
		
		@JsonProperty
		private String base64Data;		
		
	}
	
	@RequestMapping(path="/{id}/download-invoice", method=RequestMethod.POST)
	public PdfInvoiceContent downloadInvoice(@PathVariable("id") Long orderId, 
			@RequestParam(name="paymentDate", required=false) Date paymentDate,
			@RequestParam(name="r1", required=false) boolean r1,
			@RequestParam(name="oib", required=false) String oib) throws Exception {
		
		PaymentOrder paymentOrder = orderRepository.getPaymentOrderById(orderId).get();
		byte [] pdfInvoice = pdfInvoiceService.createInvoice(paymentOrder, r1, oib);
		
		PdfInvoiceContent response = new PdfInvoiceContent();
		response.fileName = paymentOrder.getInvoiceNumber() + ".pdf";
		response.base64Data = Base64.encodeBase64String(pdfInvoice);
		return response;
		
	}
	
	private boolean syncOrder(PaymentOrder order, boolean sendNotification) throws Exception {
		List<ShopifyFulfillment> fulfillments = shopifyApiClient.getFulfillments(order.getShopifyOrderId());
		sendNotification = sendNotification & !order.isPersonalTakeover();
		logger.info(MessageFormat.format("notification id: {0}, shopify: {1}, {2}", order.getShopifyOrderNumber(), order.getShopifyOrderId(), sendNotification));
		if (CollectionUtils.isEmpty(fulfillments)) {
			shopifyApiClient.fulfillOrder(order.getShopifyOrderId(), order.getTrackingNumber(), sendNotification);
			return true;
		} else {
			shopifyApiClient.updateFulfillment(order.getShopifyOrderId(), fulfillments.get(0).id, order.getTrackingNumber(), sendNotification);
		}
		return false;
	}
	
	private void sendEmail(String email, String invoiceNumber, byte[] pdfInvoice) throws Exception {
		
		MailReceipient to = new MailReceipient(email);
		if (StringUtils.isNotBlank(alwaysBcc)) {
			to.bcc(alwaysBcc);
		}
		
		MailAttachment attachment = new MailAttachment()
				.filename(invoiceNumber + ".pdf")
				.mimeType("application/pdf")
				.content(new ByteArrayInputStream(pdfInvoice));	
		
		mailService.sendEmail(to, subject, body, Collections.singletonList(attachment));
	}
	
	
	@RequestMapping(path="/{id}/refund", method=RequestMethod.POST)
	@Transactional
	public void refundOrder(@PathVariable("id") Long orderId, @RequestParam(name="itemIds", required=true) List<Long> itemIds) throws Exception {
		if (CollectionUtils.isEmpty(itemIds)) {
			throw new RuntimeException("No items specified");
		}
		PaymentOrder paymentOrder = orderRepository.getPaymentOrderById(orderId).get();
		if (!paymentOrder.isPaid()) {
			throw new RuntimeException("Order is not paid!");
		}
		
		Refund refund = paymentOrder.createRefund(itemIds);
		
		RefundInvoice invoice = refundService.createInvoice(refund);
		refund.setInvoice(invoice);
		refundRepository.save(refund);
		orderRepository.save(paymentOrder);
		byte [] pdfInvoice = pdfRefundService.createInvoice(refund);
		
		sendRefundEmail(paymentOrder.getEmail(), invoice.getNumber(), pdfInvoice);
		
	}
	
	@RequestMapping(path="/{id}/refund-custom", method=RequestMethod.POST)
	public void refundOrderCustom(@PathVariable("id") Long orderId, @RequestBody List<Item> items) throws Exception {
		if (CollectionUtils.isEmpty(items)) {
			throw new RuntimeException("No items specified");
		}
		PaymentOrder paymentOrder = orderRepository.getPaymentOrderById(orderId).get();
		if (!paymentOrder.isPaid()) {
			throw new RuntimeException("Order is not paid!");
		}
		
		Refund refund = new Refund(paymentOrder, items);
		
		RefundInvoice invoice = refundService.createInvoice(refund);
		refund.setInvoice(invoice);
		byte [] pdfInvoice = pdfRefundService.createInvoice(refund);
		refundRepository.save(refund);
		sendRefundEmail(paymentOrder.getEmail(), invoice.getNumber(), pdfInvoice);
		
	}
	
	private void sendRefundEmail(String email, String invoiceNumber, byte[] pdfInvoice) throws Exception {
		
		MailReceipient to = new MailReceipient(email);
		if (StringUtils.isNotBlank(alwaysBcc)) {
			to.bcc(alwaysBcc);
		}
		
		MailAttachment attachment = new MailAttachment()
				.filename(invoiceNumber + ".pdf")
				.mimeType("application/pdf")
				.content(new ByteArrayInputStream(pdfInvoice));	
		
		mailService.sendEmail(to, subject, refundBody, Collections.singletonList(attachment));
	}
	
	@RequestMapping(path="/{id}/duplicate", method=RequestMethod.POST)
	public void duplicateOrder() {
		
	}
	
}
