package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Collections;
import java.util.List;

import javax.websocket.server.PathParam;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollect;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollection;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyUpdateVariantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("manager/shopify-products")
@RestController
public class ShopifyProductsController {

	@Autowired
	private ShopifyApiClient apiClient;
	
	
	@RequestMapping(method=RequestMethod.GET)
	public List<ShopifyProduct> getProducts(@RequestParam("title") String title) throws Exception {
		if (StringUtils.isBlank(title) || title.trim().length() < 3) {
			return Collections.emptyList();
		}
		return apiClient.getProducts(title, 10);
	}
	
	
	
	@RequestMapping(path="/all",method=RequestMethod.GET)
	public List<ShopifyProduct> getAllProducts() throws Exception {
		return apiClient.getProducts(null);
		
	}
	
	@RequestMapping(path="/variants/{id}",method=RequestMethod.POST)
	public void updateVariant(@PathVariable("id") String id, @RequestBody ShopifyUpdateVariantRequest request) throws Exception {
		
		apiClient.updateProductVariant(id, request);
	}
	
	
}
