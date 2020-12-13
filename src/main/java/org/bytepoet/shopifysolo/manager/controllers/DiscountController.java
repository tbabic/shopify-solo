package org.bytepoet.shopifysolo.manager.controllers;

import org.bytepoet.shopifysolo.manager.models.Discount;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateDiscount;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreatePriceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/discounts")
public class DiscountController {

	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@RequestMapping(method=RequestMethod.POST)
	public void createDiscount(@RequestBody Discount discount) throws Exception {
		String discountValue = "-" + discount.getAmount();
		ShopifyCreatePriceRule priceRule = new ShopifyCreatePriceRule(discount.getCode(), discountValue);
		String priceRuleId = shopifyApiClient.createPriceRule(priceRule);
		shopifyApiClient.createDiscount(priceRuleId, new ShopifyCreateDiscount(discount.getCode()));
	}
}
