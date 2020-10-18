package org.bytepoet.shopifysolo.manager.controllers;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Inventory;
import org.bytepoet.shopifysolo.manager.repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/inventory")
public class InventoryController {

	@Autowired
	private InventoryRepository inventoryRepository;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<Inventory> getInventory(@RequestParam(value = "search", required = false) String search) {
		
		Sort sort = Sort.by(Direction.DESC, "item");
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
	
}
