package org.bytepoet.shopifysolo.controllers;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.mappers.ShopifyToSoloInvoiceMapper;
import org.bytepoet.shopifysolo.mappers.ShopifyToSoloTenderMapper;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.MailService;
import org.bytepoet.shopifysolo.services.MailService.MailAttachment;
import org.bytepoet.shopifysolo.services.MailService.MailReceipient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RequestMapping("/orders")
@RestController
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private ShopifyToSoloInvoiceMapper invoiceMapper;
	
	@Autowired
	private ShopifyToSoloTenderMapper tenderMapper;
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private MailService mailService;
	
	@Value("${email.subject}")
	private String subject;
	
	@Value("${email.body}")
	private String body;
	
	@Value("${email.always-bcc:}")
	private String alwaysBcc;
	
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder order, ContentCachingRequestWrapper request) throws Exception {
		logger.debug(order.toString());
		authorizationService.processRequest(request);
		CachedFunctionalService.<ShopifyOrder>cacheAndExecute(
				order, 
				o -> o.getId(), 
				o -> this.createInvoice(o));
		
	}
	
	
	private void createInvoice(ShopifyOrder order) {
		SoloInvoice invoice = invoiceMapper.map(order);
		String pdfUrl = soloApiClient.createInvoice(invoice);
		sendEmailWithPdf(invoice, pdfUrl);
	}
	
	private void createTender(ShopifyOrder order) {
		SoloTender tender = tenderMapper.map(order);
		String pdfUrl = soloApiClient.createTender(tender);
		sendEmailWithPdf(tender, pdfUrl);
	}


	private void sendEmailWithPdf(SoloBillingObject invoice, String pdfUrl) {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(pdfUrl).get().build();
		try {
			Response response = client.newCall(request).execute();
			String fileName = extractFileNameFromContentDisposition(response.header("Content-Disposition"));
			MailAttachment attachment = new MailAttachment()
					.filename(fileName)
					.mimeType("application/pdf")
					.content(response.body().byteStream());		
			
			MailReceipient to = new MailReceipient(invoice.getEmail());
			if (StringUtils.isNotBlank(alwaysBcc)) {
				to.bcc(alwaysBcc);
			}
			mailService.sendEmail(to, subject, body, Arrays.asList(attachment));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private String extractFileNameFromContentDisposition(String contentDispositionHeader) {
		return contentDispositionHeader.replaceFirst("attachment;filename=", "").replace("\"", "");
	}
	
	
}
