package org.bytepoet.shopifysolo.controllers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.ContentCachingRequestWrapper;



@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class OrderControllerTests {

	@MockBean
	private SoloApiClient soloApiClient;
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@Autowired
	private OrderController orderController;
	
	@Before
	public void setup() {
		CachedFunctionalService.clearCache();
	}
	
	@Test
	public void postOrder_OneValidOrder_Ok() throws Exception {
		ShopifyOrder order = ShopifyOrderCreator.createOrder("1");
		orderController.postOrder(order, null);
		Mockito.verify(soloApiClient).createInvoice(Mockito.any());
	}
	
	@Test
	public void postOrder_ValidTwoDifferentOrders_Ok() throws Exception {
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("2");
		orderController.postOrder(order1, null);
		orderController.postOrder(order2, null);
		Mockito.verify(soloApiClient, Mockito.times(2)).createInvoice(Mockito.any());
		
	}
	
	@Test
	public void postOrder_ValidTwoSameOrders_Ok() throws Exception {
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		orderController.postOrder(order1, null);
		orderController.postOrder(order2, null);
		Mockito.verify(soloApiClient, Mockito.times(1)).createInvoice(Mockito.any());
		
	}
	
	@Test
	public void postOrder_ValidTwoDifferentAsyncOrders_Ok() throws Exception {
		delayCreateReceipt(1000);
		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("2");
		CompletableFuture<Boolean> result1 = postOrderAsync(order1, null);
		CompletableFuture<Boolean> result2 = postOrderAsync(order2, null);
		assertThat(result1.get(), equalTo(true));
		assertThat(result2.get(), equalTo(true));
		Mockito.verify(soloApiClient, Mockito.times(2)).createInvoice(Mockito.any());
		
	}
	
	@Test
	public void postOrder_ValidTwoSameAsyncOrders_Ok() throws Exception {
		delayCreateReceipt(1000);
		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		CompletableFuture<Boolean> result1 = postOrderAsync(order1, null);
		CompletableFuture<Boolean> result2 = postOrderAsync(order2, null);
		assertThat(result1.get(), equalTo(true));
		assertThat(result2.get(), equalTo(true));
		Mockito.verify(soloApiClient, Mockito.times(1)).createInvoice(Mockito.any());
	}

	private void delayCreateReceipt(long sleepTime) {
		Mockito.doAnswer((Answer<?>) invocation -> {
	        Thread.sleep(sleepTime);
	        return null;
	    }).when(soloApiClient).createInvoice(Mockito.any());
	}
	
	
	private CompletableFuture<Boolean> postOrderAsync(ShopifyOrder order, ContentCachingRequestWrapper request) throws Exception {
		CompletableFuture<Boolean> result = CompletableFuture.supplyAsync(() -> {
			Boolean success = true;
			try {
				orderController.postOrder(order, null);
			} catch (Exception e) {
				success = false;
			}
			return success;
			}
		);
		return result;
	}
	
	

}
