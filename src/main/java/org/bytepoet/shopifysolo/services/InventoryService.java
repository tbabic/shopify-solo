package org.bytepoet.shopifysolo.services;

import java.io.IOException;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Inventory;
import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.repositories.InventoryRepository;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyInventoryAdjustment;
import org.bytepoet.shopifysolo.shopify.models.ShopifyLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
	
	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Autowired
	private ShopifyApiClient apiClient;
	
	@Value("${shopify.inventory.location:}")
	private String shopifyInventoryLocation;

	@Transactional
	@Deprecated
	public void moveQuantity( Long inventoryId, String shopifyInventoryId, int quantity) throws Exception {
		ShopifyInventoryAdjustment inventoryAdjustment = new ShopifyInventoryAdjustment();
		inventoryAdjustment.availableAdjustment = quantity;
		inventoryAdjustment.inventoryItemId = shopifyInventoryId;
		if (StringUtils.isBlank(shopifyInventoryLocation)) {
			List<ShopifyLocation> locations = apiClient.getLocations();
			if (locations.isEmpty()) {
				throw new RuntimeException("No existing location");
			}
			this.shopifyInventoryLocation = locations.get(0).id;
		}
		inventoryAdjustment.locationId = this.shopifyInventoryLocation;
		
		if (inventoryId != null) {
			Inventory inventory = inventoryRepository.findById(inventoryId).get();
			inventory.changeQuantity(-quantity);
			inventoryRepository.save(inventory);
		}
		
		apiClient.adjustInventory(inventoryAdjustment);
		return;
	}
	
	
	public void moveQuantity(String shopifyInventoryId, int quantity) throws Exception {
		ShopifyInventoryAdjustment inventoryAdjustment = new ShopifyInventoryAdjustment();
		inventoryAdjustment.availableAdjustment = quantity;
		inventoryAdjustment.inventoryItemId = shopifyInventoryId;
		if (StringUtils.isBlank(shopifyInventoryLocation)) {
			List<ShopifyLocation> locations = apiClient.getLocations();
			if (locations.isEmpty()) {
				throw new RuntimeException("No existing location");
			}
			this.shopifyInventoryLocation = locations.get(0).id;
		}
		inventoryAdjustment.locationId = this.shopifyInventoryLocation;
		apiClient.adjustInventory(inventoryAdjustment);
		return;
		
	}
}
