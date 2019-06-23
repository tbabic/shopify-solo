package org.bytepoet.shopifysolo.controllers;

import org.bytepoet.shopifysolo.authorization.AuthorizationService;
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
	
	@Value("${email.subject}")
	private String subject;
	
	@Value("${email.body}")
	private String body;
	
	
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder order, ContentCachingRequestWrapper request) throws Exception {
		logger.debug(order.toString());
		authorizationService.processRequest(request);
		CachedFunctionalService.<ShopifyOrder>cacheAndExecute(
				order, 
				o -> "orders/"+o.getId(), 
				o -> this.createInvoice(o));
		
	}
	
	
	private void createInvoice(ShopifyOrder order) {
		SoloInvoice invoice = invoiceMapper.map(order);
		String pdfUrl = soloApiClient.createInvoice(invoice);
		soloMaillingService.sendEmailWithPdf(invoice.getEmail(), pdfUrl, subject, body);
	}
	
}
