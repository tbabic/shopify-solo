package org.bytepoet.shopifysolo.manager.models;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bytepoet.shopifysolo.shopify.models.ShopifyLineItem;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ItemTest {

	
	@Test
	public void testCorrectTaxes_FromShopifyLineItem() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("title", "item-title");
		map.put("quantity", "1");
		
		Map<String, Object> priceSet = new LinkedHashMap<String, Object>();
		Map<String, Object> pricing = new LinkedHashMap<String, Object>();
		pricing.put("amount", "109.65");
		priceSet.put("shop_money", pricing);
		map.put("price_set", priceSet);
		
		List<Map<String, Object>> discountAllocations = new ArrayList<Map<String, Object>>();
		map.put("discount_allocations", discountAllocations);
		
		
		ObjectMapper mapper = new ObjectMapper();
		ShopifyLineItem lineItem = mapper.convertValue(map, ShopifyLineItem.class);
		Item item = new Item(lineItem, "25");
		
		Assert.assertThat(item.getPrice(), equalTo("87.72"));
		Assert.assertThat(item.getTaxRate(), equalTo("25"));
	}
	
	@Test
	public void testCorrectTaxes_FromParams_withDiscount() {
		Item item = new Item("item-name", "129.00", 1, "15.00", "25");
		Assert.assertThat(item.getPrice(), equalTo("103.20"));
		Assert.assertThat(item.getTaxRate(), equalTo("25"));
	}
	
	@Test
	public void testCorrectTaxes_FromParams_withoutDiscount() {
		Item item = new Item("item-name", "100", 1, "0.00", "25");
		Assert.assertThat(item.getPrice(), equalTo("80.00"));
		Assert.assertThat(item.getTaxRate(), equalTo("25"));
	}
	
	@Test
	public void testCorrectTaxes_FromParams_withLargePrice() {
		Item item = new Item("item-name", "100000", 1, "0.00", "25");
		Assert.assertThat(item.getPrice(), equalTo("80000.00"));
		Assert.assertThat(item.getTaxRate(), equalTo("25"));
	}
}
