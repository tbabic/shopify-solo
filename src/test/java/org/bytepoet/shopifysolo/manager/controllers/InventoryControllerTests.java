package org.bytepoet.shopifysolo.manager.controllers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Arrays;
import java.util.List;

import org.bytepoet.shopifysolo.manager.models.Inventory;
import org.bytepoet.shopifysolo.manager.models.Inventory.LinkContainer;
import org.bytepoet.shopifysolo.manager.repositories.InventoryRepository;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class InventoryControllerTests {

	@Autowired
	private InventoryController inventoryController;
	
	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Before
	public void init() {
		inventoryRepository.deleteAll();
	}
	
	@Test
	public void test_saveAndUpdateInventory( ) {
		Inventory inventory = new Inventory();
		inventory.setItem("Item");
		inventory.setLinks(Arrays.asList(LinkContainer.create("l1", "link1"), LinkContainer.create("l2", "link2")));
		
		Inventory savedInventory = inventoryController.save(inventory);
		Assert.assertThat(savedInventory.getItem(), equalTo("Item"));
		inventory.setLinks(Arrays.asList(LinkContainer.create("l1", "link1"), LinkContainer.create("l2", "link2")));
		
		inventory = new Inventory();
		inventory.setId(savedInventory.getId());
		inventory.setItem("Updated item");
		inventory.setLinks(Arrays.asList(LinkContainer.create("l1", "Updated1"), LinkContainer.create("l2", "Updated2")));
		
		Inventory updatedInventory = inventoryController.save(inventory);
		Assert.assertThat(updatedInventory.getItem(), equalTo("Updated item"));
		Assert.assertThat(updatedInventory.getLinks().size(), equalTo(2));

		
		Assert.assertThat(inventoryRepository.count(), equalTo(1L));
	}
	
	@Test
	public void test_getInventory( ) {
		Inventory inventory1 = new Inventory();
		inventory1.setItem("Item");
		inventory1.setLinks(Arrays.asList(LinkContainer.create("l1", "link1"), LinkContainer.create("l2", "link2")));
		inventoryController.save(inventory1);
		
		Inventory inventory2 = new Inventory();
		inventory2.setItem("Second item");
		inventory2.setLinks(Arrays.asList(LinkContainer.create("l3", "link3"), LinkContainer.create("l4", "link4")));
		inventoryController.save(inventory2);
		
		List<Inventory> inventoryList = inventoryController.getInventory(null);
		Assert.assertThat(inventoryList.size(), equalTo(2));
		assertInventory(inventoryList.get(0), inventory1);
		assertInventory(inventoryList.get(1), inventory2);
		
		inventoryList = inventoryController.getInventory("item");
		Assert.assertThat(inventoryList.size(), equalTo(2));
		assertInventory(inventoryList.get(0), inventory1);
		assertInventory(inventoryList.get(1), inventory2);
		
		inventoryList = inventoryController.getInventory("second");
		Assert.assertThat(inventoryList.size(), equalTo(1));
		assertInventory(inventoryList.get(0), inventory2);
		
		Assert.assertThat(inventoryRepository.count(), equalTo(2L));
	}
	
	
	private void assertInventory(Inventory actual, Inventory expected) {
		Assert.assertThat(actual.getItem(), equalTo(expected.getItem()));
		//Assert.assertThat(actual.getLinks(), equalTo(expected.getLinks()));
	}
}
