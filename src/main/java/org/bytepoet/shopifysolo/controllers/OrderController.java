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
		CachedFunctionalService.<ShopifyOrder>cacheAndExecute(
				order, 
				o -> "orders/"+o.getId(), 
				o -> this.createInvoice(o));
		
	}
	
	
	private void createInvoice(ShopifyOrder shopifyOrder) {
		SoloInvoice invoice = invoiceMapper.map(shopifyOrder);
		SoloInvoice createdInvoice = soloApiClient.createInvoice(invoice);
		try {
			//soloMaillingService.sendEmailWithPdf(invoice.getEmail(), alwaysBcc, pdfUrl, subject, body);
			//TODO: 
			
			/*CompletableFuture.runAsync(() -> {
				soloMaillingService.sendEmailWithPdf(invoice.getEmail(), alwaysBcc, createdInvoice.getPdfUrl(), subject, body);
			});*/
			CompletableFuture.runAsync(() -> {
				PaymentOrder order = new PaymentOrder(shopifyOrder, null, createdInvoice, null);
				orderRepository.save(order);
				//soloMaillingService.sendEmailWithPdf(invoice.getEmail(), alwaysBcc, createdInvoice.getPdfUrl(), subject, body);
				order.setReceiptSent(true);
				orderRepository.save(order);
			});

		} catch(Exception e) {
			logger.error(e.getMessage(),e);
		}
		
	}
	
}
