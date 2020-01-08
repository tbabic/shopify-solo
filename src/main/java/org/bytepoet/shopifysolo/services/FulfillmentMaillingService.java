package org.bytepoet.shopifysolo.services;

import java.text.MessageFormat;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentMaillingService {

	@Autowired
	private MailService mailService;
	
	@Value("${email.fulfillment.subject}")
	private String subject;
	
	@Value("${email.fulfillment.body}")
	private String body;
	
	public void sendFulfillmentEmail(String email, String trackingNumber) {
		String body = MessageFormat.format(this.body, trackingNumber);
		mailService.sendEmail(email, subject, body, Collections.emptyList());
	}
	
}
