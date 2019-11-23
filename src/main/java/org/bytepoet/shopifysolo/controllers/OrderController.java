package org.bytepoet.shopifysolo.controllers;

import java.util.Arrays;
import java.util.List;

import java.util.concurrent.CompletableFuture;

import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.mappers.ShopifyToSoloInvoiceMapper;
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
	private ShopifyToSoloInvoiceMapper invoiceMapper;

	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Value("${email.subject}")
	private String subject;
	
	@Value("${email.body}")
	private String body;
	
	@Value("${ignore.receipt}")
	private String ignoreReceipts;
	
	@Value("${email.always-bcc:}")
	private String alwaysBcc;
	
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder order, ContentCachingRequestWrapper request) throws Exception {
		List<String> ignoreReceiptsList = Arrays.asList(ignoreReceipts.split(","));
		if (ignoreReceiptsList.contains(order.getNumber())) {
			return;
		}
		logger.debug(order.toString());
		authorizationService.processRequest(request);
		SoloInvoice createdInvoice = CachedFunctionalService.<ShopifyOrder,SoloInvoice>cacheAndExecute(
				shopifyOrder, 
				o -> "orders/"+o.getId(), 
				o -> {
					SoloInvoice invoice = invoiceMapper.map(shopifyOrder);
					return soloApiClient.createInvoice(invoice);
				});
		CompletableFuture.runAsync(() -> {
			if (orderRepository.getAllWhere( o -> o.matchShopifyOrder(shopifyOrder.getId())).size() > 0) {
				return;
			}
			PaymentOrder order = new PaymentOrder(shopifyOrder, createdInvoice);
			orderRepository.save(order);
			soloMaillingService.sendEmailWithPdf(createdInvoice.getEmail(), alwaysBcc, createdInvoice.getPdfUrl(), subject, body);
			order.setReceiptSent(true);
			orderRepository.save(order);
		});
		
		return;
	}
	
}
