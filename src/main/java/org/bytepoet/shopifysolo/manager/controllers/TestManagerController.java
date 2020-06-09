package org.bytepoet.shopifysolo.manager.controllers;

import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.FulfillmentMaillingService;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/manager/test")
public class TestManagerController {
	
	
	@Value("${email.subject}")
	private String invoiceSubject;
	
	@Value("${email.body}")
	private String invoiceBody;
	
	@Value("${email.tender-body}")
	private String tenderBody;
	
	@Value("${email.tender-subject}")
	private String tenderSubject;
	
	@Autowired 
	private FulfillmentMaillingService fulfillmentMaillingService;
	
	@Autowired
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private SoloApiClient soloApiClient;
	
	public static class TestBody {
		@JsonProperty
		private long orderId;
		@JsonProperty
		private String email;
		@JsonProperty
		private String bcc;
	}
	
	@PostMapping("/email-tender")
	public void testSendTenderEmail(@RequestBody TestBody testBody) {
		PaymentOrder order = orderRepository.getPaymentOrderById(testBody.orderId).get();
		String pdfUrl = null;
		if (order.getTenderId()!= null) {
			pdfUrl = soloApiClient.getTender(order.getTenderId()).getPdfUrl();
		} else if (order.getTenderId() != null) {
			pdfUrl = soloApiClient.getInvoice(order.getInvoiceId()).getPdfUrl();
		}
		soloMaillingService.sendEmailWithPdf(testBody.email, testBody.bcc, pdfUrl, tenderSubject, tenderBody);
	}
	
	
	@PostMapping("/email-invoice")
	public void testSendReceiptEmail(@RequestBody TestBody testBody) {
		PaymentOrder order = orderRepository.getPaymentOrderById(testBody.orderId).get();
		String pdfUrl = null;
		if (order.getInvoiceId()!= null) {
			pdfUrl = soloApiClient.getInvoice(order.getInvoiceId()).getPdfUrl();
		} else if (order.getTenderId() != null) {
			pdfUrl = soloApiClient.getTender(order.getTenderId()).getPdfUrl();
		}
		soloMaillingService.sendEmailWithPdf(testBody.email, testBody.bcc, pdfUrl, invoiceSubject, invoiceBody);
	}
	
	@PostMapping("/email-shipping")
	public void testSendShippingEmail(@RequestBody TestBody testBody) {
		fulfillmentMaillingService.sendFulfillmentEmail(testBody.email, "RF123456789HR");
	}
	

}
