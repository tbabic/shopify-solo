package org.bytepoet.shopifysolo.manager.controllers;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.controllers.InventoryController.MoveQuantity;
import org.bytepoet.shopifysolo.manager.models.Inventory;
import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.models.ProductPart;
import org.bytepoet.shopifysolo.manager.models.ProductPartDistribution;
import org.bytepoet.shopifysolo.manager.repositories.ProductPartDistributionRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductPartRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductRepository;
import org.bytepoet.shopifysolo.services.InventoryService;
import org.bytepoet.shopifysolo.services.TransactionalService;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyInventoryAdjustment;
import org.bytepoet.shopifysolo.shopify.models.ShopifyLocation;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProductVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@RequestMapping("manager/inventory")
@RestController
public class InventoryProductsController {
	
	private static final Logger logger = LoggerFactory.getLogger(InventoryProductsController.class);

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductPartRepository productPartRepository;
	
	@Autowired
	private ProductPartDistributionRepository productPartDistributionRepository;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@Autowired
	private InventoryService inventoryService;
	
	@Autowired
	private TransactionalService transactionalService;
	
	public static class Inventory {
		@JsonProperty
		public List<Product> products;
		@JsonProperty
		public List<ProductPartDistribution> distributions;
		@JsonProperty
		public List<ProductPart> parts;
	}
	
	
	
	@RequestMapping(path="", method = RequestMethod.GET)
	public Inventory getInventory(
			@RequestParam(name="productsFilter", required = false) String productsFilter, 
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
		inventory.products = products;
		
		//syncWebshopInfo(products);

		return inventory;
	}
	
	
	@RequestMapping(path="/sync-products", method = RequestMethod.POST)
	public void syncProducts() throws Exception {
		List<Product> products = productRepository.findAll();
		
		syncWebshopInfo(products);

		
	}
	
	@RequestMapping(path="/products", method = RequestMethod.GET)
	public List<Product> getProducts(
			@RequestParam(name="nameFilter", required = false) String nameFilter,
			@RequestParam(name="includeWebshop", required = false) Boolean includeWebshop) throws Exception {
		List<Product> products;
			
		if (StringUtils.isBlank(nameFilter)) {
			products = productRepository.findAndFetchAll();
		} else {
			products =  productRepository.findAndFetchByNameLikeIgnoreCase("%"+ nameFilter +"%", Sort.unsorted());
		}
		
		fetchWebshopProducts(products);
		
		return products;
	}
	
	@RequestMapping(path="/products/query", method = RequestMethod.POST)
	public List<Product> getProducts(@RequestBody List<UUID> productIds) throws Exception {
		List<Product> products;
			
		products = productRepository.findAndFetchByIds(productIds);
		//syncWebshopInfo(products);
		
		return products;
	}
	
	@RequestMapping(path="/products/{id}", method = RequestMethod.GET)
	public Product getProduct(@PathVariable UUID productId) throws Exception {		
		return productRepository.findAndFetchById(productId).get();
	}
	
	public static class Query {
		@JsonProperty
		List<UUID> productIds;
		@JsonProperty
		List<UUID> partIds;
	}
	
	@RequestMapping(path="/query", method = RequestMethod.POST)
	public Inventory queryInventory(@RequestBody Query query) throws Exception {
		Inventory inventory = new Inventory();
		if (!CollectionUtils.isEmpty(query.productIds)) {
			inventory.products = productRepository.findAndFetchByIds(query.productIds);
			
		}
		if (!CollectionUtils.isEmpty(query.partIds)) {
			inventory.parts = productPartRepository.findAndFetchByIds(query.partIds);
		}		
		return inventory;
	}




	private void syncWebshopInfo(Collection<Product> products) throws Exception {
		List<Product> toSync = fetchWebshopProducts(products);
		
		productRepository.saveAll(toSync);
	}


	private List<Product> fetchWebshopProducts(Collection<Product> products) throws Exception {
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
		
		List<Product> toSync = products.stream().filter(p -> !p.isSynced()).collect(Collectors.toList());
		
		
		variants.values().forEach( v -> {
			products.add(Product.createFromWebshop(v, shopifyProductsMap.get(v.id)));
		});
		return toSync;
	}
	
	@RequestMapping(path="/products", method = RequestMethod.POST)
	@Transactional
	public void saveProduct(@RequestBody Product product) throws Exception {
		if (!product.validateAvailableMaterials()) {
			throw new RuntimeException("Not enough materials");
		}
		if (product.getWebshopInfo().getQuantity() == null) {
			product.getWebshopInfo().setQuantity(0);
		}
		Optional<Product> optional = productRepository.findById(product.getId());
		if (optional.isPresent() && optional.get().getQuantity() == product.getQuantity()) {
			productRepository.save(product);
			return;
		} 
		productRepository.save(product);
		updateWebshopQuantity(product);
		return;
	}
	
	
	public static class MoveQuantity {
		public UUID productId;
		public int quantity;
	}
	
	

	@RequestMapping(path="/move-quantity", method = RequestMethod.POST)
	public void moveQuantity( @RequestBody MoveQuantity moveQuantity) throws Exception {
		Product product = productRepository.findAndFetchById(moveQuantity.productId).get();
		product.moveQuantity(moveQuantity.quantity);
		updateWebshopQuantity(product);
		productRepository.save(product);
		
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
	}
	
	public static class ProductPartAndDistributions {
		@JsonProperty
		private ProductPart productPart;
		
		@JsonProperty
		private List<SimpleDistribution> distributions;
	}
	
	@RequestMapping(path="/parts", method = RequestMethod.POST)
	@Transactional
	public void saveProductPart(@RequestBody ProductPartAndDistributions productPartAndDistributions) {
		ProductPart part = productPartRepository.save(productPartAndDistributions.productPart);
		
		
		Map<UUID, SimpleDistribution> distroMap = productPartAndDistributions.distributions.stream().collect(Collectors.toMap(d -> d.id, d-> d));
		
		List<ProductPartDistribution> distributions = productPartDistributionRepository.findAllById(distroMap.keySet());
		List<ProductPartDistribution> distributionsToSave = new ArrayList<>();
		distributions.forEach(d -> {
			SimpleDistribution simple = distroMap.get(d.getId());
			if (d.getPartsUsed() != simple.partsUsed) {
				d.setPartsUsed(simple.partsUsed);;
			}
			distributionsToSave.add(d);
		});
		
		
		
		productPartDistributionRepository.saveAll(distributionsToSave);
		
		return;
	}
	
	
	

	private void updateWebshopQuantity(Product product) throws Exception {
		ShopifyProductVariant variant = shopifyApiClient.getShopifyVariant(product.getWebshopId());
		int quantity = product.getQuantity() - variant.quantity.intValue(); 
		
		inventoryService.moveQuantity(variant.inventoryItemId, quantity);
		return;
	}
	
	public static class AutoAdjustmentProduct {
		public UUID productId;
		public int assignedQuantity;
		public int availableAssignments;
		@JsonIgnore
		private Product product;
	}
	
	@RequestMapping(path="/auto-assignment", method = RequestMethod.POST)
	public void autoAssignment() throws Exception {
		List<Product> products = productRepository.findAndFetchAll();
		boolean assigned = true;
		Map<UUID, AutoAdjustmentProduct> map = new HashMap<>();
		while (assigned) {
			assigned = false;
			for (Product product : products) {
				if (!"active".equals( product.getWebshopInfo().getStatus())) {
					continue;
				}
				try {
					AutoAdjustmentProduct adjustment = map.get(product.getId());
					if (adjustment == null) {
						adjustment = new AutoAdjustmentProduct();
						adjustment.productId = product.getId();
						adjustment.product = product;
						map.put(adjustment.productId, adjustment);
					}
					if (adjustment.assignedQuantity >= 10) {
						continue;
					}
					product.moveQuantity(1);
					assigned = true;
					
					adjustment.assignedQuantity++;
				} catch (Exception e) {
					assigned = false;
				}
			}
		}
		
		List<ShopifyProduct> shopifyProducts = shopifyApiClient.getProducts(null);
		Map<String,ShopifyProductVariant> shopifyMap = shopifyProducts.stream().flatMap(p -> p.variants.stream()).collect(Collectors.toMap(v -> v.id, v -> v));
		
		
		map.values().forEach(adjustment -> {
			adjustment.availableAssignments = adjustment.product.getMaxFreeAssignments();
		});
		for (AutoAdjustmentProduct adjustment : map.values()) {
			ShopifyProductVariant variant = shopifyMap.get(adjustment.product.getWebshopId());
			
			logger.info(MessageFormat.format("adjust {0}: {1} + {2} = {3}", 
					adjustment.product.getName(), variant.quantity, adjustment.assignedQuantity, adjustment.product.getQuantity()));
			
			inventoryService.moveQuantity(variant.inventoryItemId, adjustment.assignedQuantity);
			productRepository.updateQuantity(adjustment.product.getQuantity(), adjustment.product.getId() );
			
		}
		
		
		logger.info("done");
	}

	
	
	
	
}
