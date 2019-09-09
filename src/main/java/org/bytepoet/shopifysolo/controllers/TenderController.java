package org.bytepoet.shopifysolo.controllers;

import java.util.Arrays;
import java.util.List;

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

@RequestMapping({"/tenders", "/drafts"})
@RestController
public class TenderController {
	
	private static final Logger logger = LoggerFactory.getLogger(TenderController.class);

	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private ShopifyToSoloTenderMapper tenderMapper;
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private SoloMaillingService soloMaillingService;
	
	@Value("${shopify.bank-deposit-gateway}")
	private String bankDepositGateway;
	
	@Value("${email.tender-body}")
	private String body;
	
	@Value("${email.tender-subject}")
	private String subject;
	
	@Value("${email.tender-bcc:}")
	private String tenderBcc;
	
	@Value("${ignore.tender}")
	private String ignoreTenders;

	
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder order, ContentCachingRequestWrapper request) throws Exception {
		logger.debug(order.toString());
		List<String> ignoreTenderList = Arrays.asList(ignoreTenders.split(","));
		if (ignoreTenderList.contains(order.getNumber())) {
			return;
		}
		authorizationService.processRequest(request);
		CachedFunctionalService.<ShopifyOrder>cacheAndExecute(
				order, 
				o -> "tenders/"+o.getId(), 
				o -> this.createTender(o));
		
	}
	
	private void createTender(ShopifyOrder order) {
		if (!bankDepositGateway.equals(order.getGateway())) {
			return;
		}
		SoloTender tender = tenderMapper.map(order);
		SoloTender createdTender = soloApiClient.createTender(tender);
		try {
			soloMaillingService.sendEmailWithPdf(tender.getEmail(), tenderBcc, createdTender.getPdfUrl(), subject, body);
		} catch(Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	
	
}
