package org.bytepoet.shopifysolo.manager.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.models.ProductPart;
import org.bytepoet.shopifysolo.manager.models.ProductPartDistribution;
import org.bytepoet.shopifysolo.manager.repositories.ProductPartDistributionRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductPartRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductRepository;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RequestMapping("manager/inventory")
@RestController
public class InventoryProductsController {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductPartRepository productPartRepository;
	
	@Autowired
	private ProductPartDistributionRepository productPartDistributionRepository;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	public static class Inventory {
		List<Product> products;
		List<ProductPartDistribution> distributions;
		List<ProductPart> parts;
	}
	
	
	
	@RequestMapping(path="", method = RequestMethod.GET)
	public Inventory getInventory(
			@RequestParam(name="productsFilter", required = false) String productsFilter, 
			@RequestParam(name = "webshopInfo", required = false) boolean webshopInfo,
			@RequestParam(name="partsFilter", required = false) String partsFilter) throws Exception {
		List<Product> products;
		List<ProductPart> parts;
		List<ProductPartDistribution> distributions;
		
		if (StringUtils.isNotBlank(productsFilter) && StringUtils.isNotBlank(partsFilter)) {
			throw new RuntimeException("Can't filter by products and parts at the same time");
		}
		
		if (StringUtils.isBlank(productsFilter) && StringUtils.isBlank(partsFilter)) {
			products = productRepository.findAll();
			parts = productPartRepository.findAll();
		} else if (StringUtils.isNotBlank(productsFilter)){
			products =  productRepository.findByNameLikeIgnoreCase("%"+ productsFilter +"%", Sort.unsorted());
			parts = productPartRepository.findAll();
		} else {
			products =  productRepository.findAll();
			parts = productPartRepository.searchProductParts(partsFilter);
		}
		
		distributions = productPartDistributionRepository.findAll();
		
		Inventory inventory = new Inventory();
		inventory.parts = parts;
		inventory.distributions = distributions;
		
		if (!webshopInfo) {
			inventory.products = products;
			return inventory;
		}
		
		List<ShopifyProduct> shopifyProducts = shopifyApiClient.getProducts(null);
		
		
		
		Map<String, ShopifyProduct> shopifyProductsMap = new HashMap<>();
		Map<String, ShopifyProductVariant> variants = new HashMap<>();
		
		for (ShopifyProduct shopifyProduct : shopifyProducts) {
			for (ShopifyProductVariant variant : shopifyProduct.variants) {
				variant.title = variant.title.equals("Default Title") ? shopifyProduct.title : shopifyProduct.title + " / " + variant.title;
				variants.put(variant.id, variant);
				shopifyProductsMap.put(variant.id, shopifyProduct);
			}
		}
		
		products.forEach(product -> {
			if (StringUtils.isBlank(product.getWebshopId())){
				return;
			}
			ShopifyProductVariant variant = variants.get(product.getWebshopId());
			product.setWebshopQuantity(variant.quantity.intValue());
			product.setWebshopStatus(shopifyProductsMap.get(variant.id).status);
			variants.remove(variant.id);
		});
		
		variants.values().forEach( v -> {
			products.add(Product.createFromWebshop(v, shopifyProductsMap.get(v.id)));
		});
		
		
		inventory.products = products;
		return inventory;
	}
	
	
	
	
	@RequestMapping( method = RequestMethod.POST)
	@Transactional
	public void saveInventory(@RequestBody Inventory inventory) {
		productRepository.saveAll(inventory.products);
		productPartRepository.saveAll(inventory.parts);
		productPartDistributionRepository.saveAll(inventory.distributions);
		
		return;
	}
	
	
	@RequestMapping(path="/products", method = RequestMethod.GET)
	public List<Product> getProducts(
			@RequestParam(name="nameFilter", required = false) String nameFilter, 
			@RequestParam(name = "webshopInfo", required = false) boolean webshopInfo) throws Exception {
		List<Product> products;
			
		if (StringUtils.isBlank(nameFilter)) {
			products = productRepository.findAndFetchAll();
		} else {
			products =  productRepository.findAndFetchByNameLikeIgnoreCase("%"+ nameFilter +"%", Sort.unsorted());
		}
		
		
		if (!webshopInfo) {
			return products;
		}
		
		List<ShopifyProduct> shopifyProducts = shopifyApiClient.getProducts(null);
		
		Map<String, ShopifyProduct> shopifyProductsMap = new HashMap<>();
		Map<String, ShopifyProductVariant> variants = new HashMap<>();
		
		for (ShopifyProduct shopifyProduct : shopifyProducts) {
			for (ShopifyProductVariant variant : shopifyProduct.variants) {
				variant.title = variant.title.equals("Default Title") ? shopifyProduct.title : shopifyProduct.title + " / " + variant.title;
				variants.put(variant.id, variant);
				shopifyProductsMap.put(variant.id, shopifyProduct);
			}
		}
		
	
		
		products.forEach(product -> {
			if (StringUtils.isBlank(product.getWebshopId())){
				return;
			}
			ShopifyProductVariant variant = variants.get(product.getWebshopId());
			product.setWebshopQuantity(variant.quantity.intValue());
			product.setWebshopStatus(shopifyProductsMap.get(variant.id).status);
			variants.remove(variant.id);
		});
		
		variants.values().forEach( v -> {
			products.add(Product.createFromWebshop(v, shopifyProductsMap.get(v.id)));
		});
		return products;
	}
	
	@RequestMapping(path="/products", method = RequestMethod.POST)
	public void saveProduct(@RequestBody Product product) {
		productRepository.save(product);
		return;
	}
	
	
	@RequestMapping(path="/parts", method = RequestMethod.GET)
	public List<ProductPart> getProductParts(
			@RequestParam(name="search", required = false) String search) {
		
		if (StringUtils.isBlank(search)) {
			return productPartRepository.findAllProductParts();
		}
		
		return productPartRepository.searchAndFetchProductParts(search);
	}
	
	public static class SimpleDistribution {
		
		@JsonProperty
		UUID id;
		
		@JsonProperty
		private int partsUsed;
		
		@JsonProperty
		private int assignedQuantity;
	}
	
	public static class ProductPartAndDistributions {
		@JsonProperty
		ProductPart productPart;
		
		@JsonProperty
		List<SimpleDistribution> distributions;
	}
	
	@RequestMapping(path="/parts", method = RequestMethod.POST)
	@Transactional
	public void saveProductPart(@RequestBody ProductPartAndDistributions productPartAndDistributions) {
		ProductPart part = productPartRepository.save(productPartAndDistributions.productPart);
		
		
		Map<UUID, SimpleDistribution> distroMap = productPartAndDistributions.distributions.stream().collect(Collectors.toMap(d -> d.id, d-> d));
		
		List<ProductPartDistribution> distributions = productPartDistributionRepository.findAllById(distroMap.keySet());
		distributions.forEach(d -> {
			SimpleDistribution simple = distroMap.get(d.getId());
			d.update(simple.assignedQuantity, simple.partsUsed);
		});
		
		
		
		productPartDistributionRepository.saveAll(distributions);
		
		return;
	}
	
	@RequestMapping(path="/transfer-from-old", method = RequestMethod.POST)
	public void transfer() {
	
	}
	
	
}
