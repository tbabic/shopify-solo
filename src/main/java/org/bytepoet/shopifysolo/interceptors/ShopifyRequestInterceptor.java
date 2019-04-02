package org.bytepoet.shopifysolo.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bytepoet.shopifysolo.controllers.OrderController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerInterceptor;

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
		String secret = request.getHeader(secretHeader);
		if (!this.secret.equals(secret)) {
			logger.debug("Secret: " + secret);
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
}
