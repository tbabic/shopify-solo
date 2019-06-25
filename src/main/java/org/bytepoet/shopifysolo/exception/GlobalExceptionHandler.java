package org.bytepoet.shopifysolo.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.services.MailService;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.ContentCachingRequestWrapper;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SoloApiClient.class);
	
	@Autowired
	private MailService mailService;
	
	@Value("${error.email}")
	private String email;
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleException(Exception e, ContentCachingRequestWrapper request) {
		
		Map<String, String> response = handleError(e, request);
		
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}

	

	@ExceptionHandler(HttpStatusCodeException.class)
	public ResponseEntity<Object> handleHttpException(HttpStatusCodeException e, ContentCachingRequestWrapper request) {
		
		Map<String, String> response = handleError(e, request);
		
		return new ResponseEntity<>(response, e.getStatusCode());
		
	}
	
	private Map<String, String> handleError(Exception e, ContentCachingRequestWrapper request) {
		Map<String, String> response = new LinkedHashMap<String, String>();
		response.put("message",e.getMessage());
		
		logger.error(e.getMessage(),e);
		String requestBody = new String(request.getContentAsByteArray());
		
		if (StringUtils.isNotBlank(email)) {
			mailService.sendEmail(email, "Error", "Dogodila se greška!\n\n"+requestBody, Collections.emptyList());
		}
		return response;
	}
	

}
