package org.bytepoet.shopifysolo.manager.controllers;

import java.io.IOException;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Inventory;
import org.bytepoet.shopifysolo.manager.repositories.InventoryRepository;
import org.bytepoet.shopifysolo.services.InventoryService;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyInventoryAdjustment;
import org.bytepoet.shopifysolo.shopify.models.ShopifyProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/old-inventory")
public class InventoryController {

	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Autowired
	private ShopifyApiClient apiClient;
	
	@Autowired
	private InventoryService inventoryService;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<Inventory> getInventory(@RequestParam(value = "search", required = false) String search) {
		
		Sort sort = Sort.by(Direction.ASC, "item");
		if (StringUtils.isBlank(search)) {
			return inventoryRepository.findAll(sort);
		}
		
		return inventoryRepository.findByItemLikeIgnoreCase("%" + search + "%", sort);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public Inventory save(@RequestBody Inventory inventory) {
		if (StringUtils.isBlank(inventory.getItem())) {
			throw new RuntimeException("Item must not be empty");
		}
		return inventoryRepository.save(inventory);
	}
	
	public static class ShopifyConnect {
		public String variantId;
		public String variantName;
	}
	
	@RequestMapping(path="/{id}", method = RequestMethod.DELETE)
	public void connect(@PathVariable("id") long id) {
		inventoryRepository.deleteById(id);
	}
	
	public static class MoveQuantity {
		public Long inventoryId;
		public String shopifyInventoryItemId;
		public int quantity;
	}

	@RequestMapping(path="/move-quantity", method = RequestMethod.POST)
	public void moveQuantity( @RequestBody MoveQuantity moveQuantity) throws Exception {
		inventoryService.moveQuantity(moveQuantity.inventoryId, moveQuantity.shopifyInventoryItemId, moveQuantity.quantity);
	}
	
	
}
