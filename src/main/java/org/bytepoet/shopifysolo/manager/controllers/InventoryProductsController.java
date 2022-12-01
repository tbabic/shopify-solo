package org.bytepoet.shopifysolo.manager.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.models.ProductPart;
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

@RequestMapping("manager/products")
@RestController
public class InventoryProductsController {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductPartRepository productPartRepository;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<Product> getProducts(
			@RequestParam(name="nameFilter", required = false) String nameFilter, 
			@RequestParam(name = "webshopInfo", required = false) boolean webshopInfo) throws Exception {
		List<Product> products;
			
		if (StringUtils.isBlank(nameFilter)) {
			products = productRepository.findAll();
		} else {
			products =  productRepository.findByNameLikeIgnoreCase("%"+ nameFilter +"%", Sort.unsorted());
		}
		
		
		if (!webshopInfo) {
			return products;
		}
		
		List<ShopifyProduct> shopifyProducts = shopifyApiClient.getProducts(null);
		
		
		
		Map<String, ShopifyProductVariant> variants = shopifyProducts.stream()
				.flatMap(shopifyProduct -> shopifyProduct.variants.stream()
						.peek(variant -> {
							variant.title = variant.title.equals("Default Title") ? shopifyProduct.title : shopifyProduct.title + " / " + variant.title;
						}))
				.collect(Collectors.toMap(variant -> variant.id, variant -> variant));
		
		products.forEach(product -> {
			if (StringUtils.isBlank(product.getWebshopId())){
				return;
			}
			ShopifyProductVariant variant = variants.get(product.getWebshopId());
			product.setWebshopQuantity(variant.quantity.intValue());
			variants.remove(variant.id);
		});
		
		variants.values().forEach( v -> {
			products.add(Product.createFromWebshop(v));
		});
		return products;
	}
	
	@RequestMapping(method = RequestMethod.POST)
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
		
		return productPartRepository.searchProductParts(search);
	}
	
	@RequestMapping(path="/parts", method = RequestMethod.POST)
	public void saveProductPart(@RequestBody ProductPart productPart) {
		productPartRepository.save(productPart);
		return;
	}
}
