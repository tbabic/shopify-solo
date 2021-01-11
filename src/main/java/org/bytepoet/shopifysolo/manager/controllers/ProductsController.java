package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("manager/products")
@RestController
public class ProductsController {

	@Autowired
	private ShopifyApiClient apiClient;
	
	
	@RequestMapping(method=RequestMethod.GET)
	public List<ShopifyProduct> getProducts(@RequestParam("title") String title) throws Exception {
		if (StringUtils.isBlank(title) || title.trim().length() < 3) {
			return Collections.emptyList();
		}
		return apiClient.getProducts(title);
	}
}
