package org.bytepoet.shopifysolo.manager.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.repositories.ProductRepository;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("manager/products")
@RestController
public class InventoryProductsController {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<Product> getProducts(String nameFilter, boolean webshopInfo) throws Exception {
		List<Product> products =  productRepository.findByNameLikeIgnoreCase(nameFilter, Sort.unsorted());
		
		if (!webshopInfo) {
			return products;
		}
		
		List<ShopifyProduct> shopifyProducts = shopifyApiClient.getProducts(null);
		Map<String, ShopifyProductVariant> variants = shopifyProducts.stream()
				.flatMap(shopifyProduct -> shopifyProduct.variants.stream())
				.collect(Collectors.toMap(variant -> variant.id, variant -> variant));
		
		products.forEach(product -> {
			ShopifyProductVariant variant = variants.get(product.getWebshopId());
			product.setWebshopQuantity(variant.quantity.intValue());
		});
		
		return products;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}
	
	
	
	
	
}
