package org.bytepoet.shopifysolo.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SoloApiClient.class);

	@ExceptionHandler(HttpStatusCodeException.class)
	public ResponseEntity<Object> handleHttpException(HttpStatusCodeException e) {
		
		Map<String, String> response = new LinkedHashMap<String, String>();
		response.put("message",e.getMessage());
		
		logger.error(e.getMessage(),e);
		
		return new ResponseEntity<>(response, e.getStatusCode());
		
	}
	
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handlexception(Exception e) {
		
		Map<String, String> response = new LinkedHashMap<String, String>();
		response.put("message",e.getMessage());
		
		logger.error(e.getMessage(),e);
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
}
