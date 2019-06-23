package org.bytepoet.shopifysolo.controllers;

import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.mappers.ShopifyToSoloTenderMapper;
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

@RequestMapping("/drafts")
@RestController
public class DraftController {
	
	private static final Logger logger = LoggerFactory.getLogger(DraftController.class);

	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private ShopifyToSoloTenderMapper tenderMapper;
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private SoloMaillingService soloMaillingService;
	
	@Value("${email.tender-body}")
	private String body;
	
	@Value("${email.tender-subject}")
	private String subject;
	

	
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder order, ContentCachingRequestWrapper request) throws Exception {
		logger.debug(order.toString());
		authorizationService.processRequest(request);
		CachedFunctionalService.<ShopifyOrder>cacheAndExecute(
				order, 
				o -> "drafts/"+o.getId(), 
				o -> this.createTender(o));
		
	}
	
	private void createTender(ShopifyOrder order) {
		SoloTender tender = tenderMapper.map(order);
		String pdfUrl = soloApiClient.createTender(tender);
		soloMaillingService.sendEmailWithPdf(tender.getEmail(), pdfUrl, subject, body);
	}

	
	
}