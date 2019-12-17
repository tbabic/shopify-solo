package org.bytepoet.shopifysolo.controllers;

import java.util.List;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.mappers.OrderToSoloTenderMapper;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;

@RequestMapping({"/tenders", "/drafts"})
@RestController
public class TenderController {
	
	private static final Logger logger = LoggerFactory.getLogger(TenderController.class);

	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private OrderToSoloTenderMapper tenderMapper;
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private SoloMaillingService soloMaillingService;

	@Autowired
	private GatewayToPaymentTypeMapper paymentTypeMapper;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Value("${shopify.bank-deposit-gateway}")
	private List<String> bankDepositGateway;
	
	@Value("${email.tender-body}")
	private String body;
	
	@Value("${email.tender-subject}")
	private String subject;
	
	@Value("${email.tender-bcc:}")
	private String tenderBcc;
	
	@Value("${ignore.tender}")
	private String ignoreTenders;

	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder shopifyOrder, ContentCachingRequestWrapper request) throws Exception {
		authorizationService.processRequest(request);
		if (!bankDepositGateway.contains(shopifyOrder.getGateway())) {
			return;
		}
		logger.debug(shopifyOrder.toString());
		PaymentOrder order;
		synchronized(this.getClass()) {
			order = orderRepository.getOrderWithShopifyId(shopifyOrder.getId()).orElseGet(() -> {
				return orderRepository.saveAndFlush(new PaymentOrder(shopifyOrder, paymentTypeMapper));
			});
		}
		
		
		if (!order.isTenderCreated()) {
			SoloTender createdTender = CachedFunctionalService.<ShopifyOrder,SoloTender>cacheAndExecute(
					shopifyOrder, 
					o -> "orders/"+o.getId(), 
					o -> {
						SoloTender tender = tenderMapper.map(order);
						return soloApiClient.createTender(tender);
					});
			order.updateFromSoloTender(createdTender);
			orderRepository.saveAndFlush(order);
			soloMaillingService.sendEmailWithPdf(order.getEmail(), tenderBcc, createdTender.getPdfUrl(), subject, body);
			order.setTenderSent(true);
			orderRepository.save(order);
		}
		
		if(!order.isTenderSent()) {
			SoloTender createdTender = soloApiClient.getTender(order.getTenderId());
			soloMaillingService.sendEmailWithPdf(order.getEmail(), tenderBcc, createdTender.getPdfUrl(), subject, body);
			order.setTenderSent(true);
			orderRepository.save(order);
		}
		
	}

	
	
}
