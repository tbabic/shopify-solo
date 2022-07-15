package org.bytepoet.shopifysolo.manager.controllers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.controllers.ShopifyOrderCreator;
import org.bytepoet.shopifysolo.controllers.TenderController;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.manager.repositories.ProductRepository;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;



@SpringBootTest
@AutoConfigureEmbeddedDatabase
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class ProductControllerTests {

	
	@Autowired
	private InventoryProductsController productController;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Before
	public void setup() {
		CachedFunctionalService.clearCache();
		productRepository.deleteAll();
		
		Product product;
		productController.saveProduct(product);
		
	}
	
	

	
}
