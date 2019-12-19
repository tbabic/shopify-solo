package org.bytepoet.shopifysolo.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.mappers.OrderToSoloInvoiceMapper;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;

@RequestMapping("/orders")
@RestController
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private OrderToSoloInvoiceMapper invoiceMapper;

	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private GatewayToPaymentTypeMapper paymentTypeMapper;
	
	@Value("${email.subject}")
	private String subject;
	
	@Value("${email.body}")
	private String body;
	
	@Value("${ignore.receipt}")
	private String ignoreReceipts;
	
	@Value("${email.always-bcc:}")
	private String alwaysBcc;
	
	@Value("${solofy.tax-rate:}")
	private String taxRate;
	
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder shopifyOrder, ContentCachingRequestWrapper request) throws Exception {
		authorizationService.processRequest(request);
		List<String> ignoreReceiptsList = Arrays.asList(ignoreReceipts.split(","));
		if (ignoreReceiptsList.contains(shopifyOrder.getNumber())) {
			return;
		}
		logger.debug(shopifyOrder.toString());
		PaymentOrder order;
		synchronized(this.getClass()) {
			order = orderRepository.getOrderWithShopifyId(shopifyOrder.getId()).orElseGet(() -> {
				return orderRepository.saveAndFlush(new PaymentOrder(shopifyOrder, paymentTypeMapper, taxRate));
			});
		}
		
		
		if (!order.isReceiptCreated()) {
			SoloInvoice createdInvoice = CachedFunctionalService.<ShopifyOrder,SoloInvoice>cacheAndExecute(
					shopifyOrder, 
					o -> "orders/"+o.getId(), 
					o -> {
						SoloInvoice invoice = invoiceMapper.map(order);
						return soloApiClient.createInvoice(invoice);
					});
			order.updateFromSoloInvoice(createdInvoice, new Date());
			orderRepository.saveAndFlush(order);
			soloMaillingService.sendEmailWithPdf(order.getEmail(), alwaysBcc, createdInvoice.getPdfUrl(), subject, body);
			order.setReceiptSent(true);
			orderRepository.save(order);
		}
		
		if(!order.isReceiptSent()) {
			SoloInvoice createdInvoice = soloApiClient.getInvoice(order.getInvoiceId());
			soloMaillingService.sendEmailWithPdf(order.getEmail(), alwaysBcc, createdInvoice.getPdfUrl(), subject, body);
			order.setReceiptSent(true);
			orderRepository.save(order);
		}
		return;
	}
	
}
