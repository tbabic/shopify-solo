package org.bytepoet.shopifysolo.manager.controllers;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.websocket.server.PathParam;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Currency;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollect;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollection;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import org.bytepoet.shopifysolo.shopify.models.ShopifyUpdateVariantRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger logger = LoggerFactory.getLogger(ShopifyProductsController.class);

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
	
	@RequestMapping(path="/convert-to-euro", method=RequestMethod.POST)
	public void convertToEuro(@RequestBody List<String> skips) throws Exception {
		logger.info("Starting conversion to euro");
		
		DecimalFormat format = new DecimalFormat("0.00");
		format.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		format.setDecimalFormatSymbols(symbols);
		
		
		List<ShopifyProduct> allProducts = apiClient.getProducts(null);
		
		
		for(ShopifyProduct product : allProducts) {
			for (ShopifyProductVariant variant : product.variants) {
				if (skips.contains(variant.id)) {
					continue;
				}
				double priceIncorrect = format.parse(variant.price).doubleValue();
				
				double priceKn = Currency.HRK.convertFrom(Currency.EUR, priceIncorrect);
				double priceEur = Currency.EUR.convertFrom(Currency.HRK, Math.round(priceKn));
				
				if (priceEur == priceIncorrect) {
					continue;
				}
				
				String formattedPrice = format.format(priceEur);
				
				if (formattedPrice.equals(variant.price)) {
					continue;
				}
				
				ShopifyUpdateVariantRequest request = ShopifyUpdateVariantRequest.create(formattedPrice, "0");
				
				logger.info(MessageFormat.format("{0}: convertStart {1}/{2}", 
						variant.id, product.title, variant.title));
				logger.info(MessageFormat.format("{0}: price: {1} -> {2}; comparePrice: {3} -> {4}", 
						variant.id, variant.price, formattedPrice, variant.compareAtPrice, "0"));
				
				apiClient.updateProductVariant(variant.id, request);
				
				logger.info(MessageFormat.format("{0}: convertEnd {1}/{2}", 
						variant.id, product.title, variant.title));
				
			}
		}
		

		
	}
	
	
	
}
