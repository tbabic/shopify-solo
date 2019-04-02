package org.bytepoet.shopifysolo.interceptors;

import java.io.InputStreamReader;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.common.io.CharStreams;

@Component
public class ShopifyRequestInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(ShopifyRequestInterceptor.class);
	
	@Value("${shopify.secret.header}")
	private String secretHeader = "X-Shopify-Hmac-Sha256";
	@Value("${shopify.secret.value}")
	private String secret = "3935b27b02d57fd3e720b9ced7517140219169a6d82b876275621bd8a7b4fbd4";
	
	@Value("${shopify.shop-domain.header}")
	private String shopDomainHeader = "X-Shopify-Shop-Domain";
	@Value("${shopify.shop-domain.value}")
	private String shopDomain = "www.kragrlica.com";
	
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (!verifySecretHeader(request)) {
			throwUnauthorizedException("Secret header is not valid");
			return false;
		}
		String shopDomain = request.getHeader(shopDomainHeader);
		if (!this.shopDomain.equals(shopDomain)) {
			logger.debug("Domain: " + shopDomain);
			throwUnauthorizedException("Shop domain is not valid");
			return false;
		}
		return true;
		
	}
	
	private void throwUnauthorizedException(String errorMessage) {
		throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, errorMessage);
	}
	
	private boolean verifySecretHeader(HttpServletRequest request) throws Exception {
		Mac alg = Mac.getInstance("HmacSHA256");
		SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		alg.init(keySpec);
		String data = CharStreams.toString(new InputStreamReader(request.getInputStream()));
		logger.debug("data: " + data);
		byte [] encodedBytes = alg.doFinal(data.getBytes());
		String encodedString = Base64.encodeBase64String(encodedBytes);
		
		String actualValue = request.getHeader(secretHeader);
		logger.debug("secret received: " + actualValue);
		if (encodedString.equals(actualValue)) {
			return true;
		}
		return false;
	}
}
