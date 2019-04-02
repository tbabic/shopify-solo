package org.bytepoet.shopifysolo.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

public class ShopifyRequestInterceptor implements HandlerInterceptor {

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
			return false;
		}
		String shopDomain = request.getHeader(shopDomainHeader);
		if (!this.shopDomain.equals(shopDomain)) {
			return false;
		}
		return true;
		
	}
}
