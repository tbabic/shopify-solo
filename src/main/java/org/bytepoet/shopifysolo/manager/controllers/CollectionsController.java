package org.bytepoet.shopifysolo.manager.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.services.AsyncService;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollect;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollection;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCollectionCustomUpdate;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("manager/collections")
@RestController
public class CollectionsController {

	@Autowired
	private ShopifyApiClient apiClient;
	
	@Autowired
	private AsyncService asyncService;
	
	
	@RequestMapping(method=RequestMethod.GET)
	public List<ShopifyCollection> getCollections() throws Exception {
		List<ShopifyCollection> smartCollections = apiClient.getShopifySmartCollections();
		List<ShopifyCollection> customCollections = apiClient.getShopifyCustomCollections();
		List<ShopifyCollection> all = new ArrayList<>();
		all.addAll(smartCollections);
		all.addAll(customCollections);
		all.sort((c1, c2) -> c1.title.compareTo(c2.title));
		return all;
		
	}
	
	@RequestMapping(path="/{id}/products",method=RequestMethod.GET)
	public List<ShopifyProduct> getCollections(@PathVariable("id") String id) throws Exception {
		return getFullCollectionProducts(id);
		
	}
	
	
	
	@RequestMapping(path="/{id}/sort",method=RequestMethod.PUT)
	public List<ShopifyProduct> updateShopifyCollectionSort(@PathVariable("id") String id, @RequestBody CollectionSort body) throws Exception {
		Future<List<ShopifyCollection>> futureSmart = asyncService.future(() -> {
			try {
				return apiClient.getShopifySmartCollections();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
		
		List<ShopifyProduct> products = getFullCollectionProducts(id);
		if (body == null || body.sorting == null || body.direction == null) {
			throw new RuntimeException("no sorting");
		}
		
		products = sort(products, body);
		List<ShopifyCollection> smartCollection= futureSmart.get();
		boolean isSmart = smartCollection.stream().filter(c -> c.id.equals(id)).findAny().isPresent();
		if (isSmart) {
			updateSmartCollectionSort(products, id);
		}else {
			updateCustomCollectionSort(products, id);
		}
		return products;
	}
	
	private void updateSmartCollectionSort(List<ShopifyProduct> sortedProducts, String id) throws IOException {
		List<String> ids = sortedProducts.stream().map(p -> p.id).collect(Collectors.toList());
		apiClient.updateShopifySmartCollectionOrder(id, "manual", ids);
	}
	
	private void updateCustomCollectionSort(List<ShopifyProduct> sortedProducts, String id) throws IOException {
		List<ShopifyCollect> collects = apiClient.getShopifyCollects(id);
		Map<String, String> map = new HashMap<String, String>(collects.size(), 1.0f);
		for (ShopifyCollect collect :collects) {
			map.put(collect.productId, collect.id);
		}
		
		ShopifyCollectionCustomUpdate custom = new ShopifyCollectionCustomUpdate();
		custom.setSortOrder("manual");
		apiClient.updateShopifyCustomCollection(id, custom);
		
		int i = 0;
		for (ShopifyProduct product : sortedProducts) {
			i++;
			ShopifyCollect collect = new ShopifyCollect();
			collect.id = map.get(product.id);
			if (collect.id == null) {
				throw new RuntimeException("Missing collect");
			}
			collect.position = String.valueOf(i);
			custom.addCollect(collect);
		}
		custom.setSortOrder(null);
		apiClient.updateShopifyCustomCollection(id, custom);
		
	}
	
	private List<ShopifyProduct> getFullCollectionProducts(String id) throws Exception {
		Future<List<ShopifyProduct>> futureCollection = asyncService.future( ()-> {
			try {
				return apiClient.getShopifyCollectionProducts(id);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
		Future<List<ShopifyProduct>> futureAll = asyncService.future( ()-> {
			try {
				return apiClient.getProducts(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		List<ShopifyProduct> collectionProducts = futureCollection.get();
		List<ShopifyProduct> allProducts = futureAll.get();
		
		List<ShopifyProduct> fullCollectionProducts = new ArrayList<>();
		for (ShopifyProduct collectionProduct : collectionProducts) {
			for (ShopifyProduct fullProduct : allProducts) {
				if (fullProduct.id.equals(collectionProduct.id)) {
					fullCollectionProducts.add(fullProduct);
				}
			}
		}
		collectionProducts.clear();
		allProducts.clear();
		return fullCollectionProducts;
	}
	
	private List<ShopifyProduct> sort(List<ShopifyProduct> products, CollectionSort body) {
		products = products.stream().sorted((p1, p2) -> {
		
			int compare = 0;
			int quantity1 = quantity(p1);
			int quantity2 = quantity(p2);
			
			if (body.sorting == Sorting.PRICE || body.sorting == Sorting.AVAILABLE_AND_PRICE) {
				double price1 = minPrice(p1);
				double price2 = minPrice(p2);
				compare = Double.compare(price1, price2);
			}
			
			else {
				double discount1 = maxDiscount(p1);
				double discount2 = maxDiscount(p2);
				compare = Double.compare(discount1, discount2);
			}
			
			if (body.direction == SortingDirection.DESCENDING) {
				compare = -1*compare;
			}
			if (compare == 0) {
				compare = Integer.compare(quantity2, quantity1);
			}
			if (body.sorting == Sorting.AVAILABLE_AND_DISCOUNT || body.sorting == Sorting.AVAILABLE_AND_PRICE) {
				int available1 = quantity1 > 0 ? 1: 0;
				int available2 = quantity2 > 0 ? 1: 0;
				int availableCompare = Integer.compare(available2, available1);
				if (availableCompare != 0) {
					compare = availableCompare;
				}
			}
			
			if (compare == 0) {
				compare = p1.title.toLowerCase().compareTo(p2.title.toLowerCase());
			}
			return compare;
			
			
		}).collect(Collectors.toList());
		return products;
	}
	
	private double minPrice(ShopifyProduct product) {
		return product.variants.stream()
				.mapToDouble(variant -> Double.parseDouble(variant.price))
				.min().getAsDouble();
	}
	
	private double maxDiscount(ShopifyProduct product) {
		return product.variants.stream().
				mapToDouble(variant -> calculateDiscount(variant))
				.max().getAsDouble();
	}
	
	private int quantity(ShopifyProduct product) {
		int quantity = product.variants.stream().mapToInt( variant -> Integer.parseInt(variant.quantity)).max().getAsInt();
		return quantity;
	}
	
	private double calculateDiscount(ShopifyProductVariant variant) {
		if (variant.compareAtPrice == null) {
			return 0;
		}
		double compareAtPrice = Double.parseDouble(variant.compareAtPrice);
		if (compareAtPrice == 0) {
			return 0;
		}
		double variantPrice = Double.parseDouble(variant.price);
		return 100 * variantPrice / compareAtPrice;
	}
	
	
	public static class CollectionSort {
		public Sorting sorting;
		public SortingDirection direction;
	}
	
	public static enum Sorting {
		PRICE,
		DISCOUNT,
		AVAILABLE_AND_PRICE,
		AVAILABLE_AND_DISCOUNT
		
	}
	
	public static enum SortingDirection {
		ASCENDING,
		DESCENDING
		
	}
}
