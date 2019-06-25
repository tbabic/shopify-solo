package org.bytepoet.shopifysolo.controllers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.junit.Assert;
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
	
	@MockBean
	private SoloMaillingService soloMaillingService;
	
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
	public void postOrder_ThreeSameOrdersFirstFails_SecondExecutedThirdSkipped() throws Exception {		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order3 = ShopifyOrderCreator.createOrder("1");
		Mockito.when(soloApiClient.createInvoice(Mockito.any()))
			.thenThrow(RuntimeException.class)
			.thenReturn(null);
		try {
			orderController.postOrder(order1, null);
			Assert.fail("Expected runtime exception");
		} catch(RuntimeException e) {
			//expected
		}
		orderController.postOrder(order2, null);
		orderController.postOrder(order3, null);
		Mockito.verify(soloApiClient, Mockito.times(2)).createInvoice(Mockito.any());
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
		
		executeMultipleAsyncOrders(2, 0, new Expectations().invocations(1).errors(0).successes(2));
	}
	
	
	@Test
	public void postOrder_TwoSameAsyncOrdersFirstFails_SecondExecuted() throws Exception {
		delayCreateReceipt(1000);

		executeMultipleAsyncOrders(2, 1, new Expectations().invocations(2).errors(1).successes(1));
	}
	
	@Test
	public void postOrder_ThreeSameAsyncOrdersFirstFails_SecondExecutedThirdSkipped() throws Exception {
		delayCreateReceipt(1000);
		executeMultipleAsyncOrders(3, 1, new Expectations().invocations(2).errors(1).successes(2));
	}
	
	@Test
	public void postOrder_TenSameAsyncOrdersFirstFiveFails_SecondExecutedThirdSkipped() throws Exception {
		delayCreateReceipt(1000);
		executeMultipleAsyncOrders(10, 5, new Expectations().invocations(6).errors(5).successes(5));
	}
	
	private void executeMultipleAsyncOrders(int createdOrders, int succesiveErrors, Expectations expected) throws Exception {
		
		List<ShopifyOrder> orders = new ArrayList<>();
		for (int i = 0; i<createdOrders; i++) {
			orders.add(ShopifyOrderCreator.createOrder("1"));
		}
		RuntimeException [] exceptions = new RuntimeException[succesiveErrors];
		for (int i = 0; i<succesiveErrors; i++) {
			exceptions[i] = new RuntimeException();
		}
		if (succesiveErrors > 0) {
			Mockito.when(soloApiClient.createInvoice(Mockito.any()))
			.thenThrow(exceptions)
			.thenReturn(null);
		} else {
			Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenReturn(null);
		}
		
		
		List<CompletableFuture<Boolean>> results = orders.stream().map(o -> postOrderAsync(o, null)).collect(Collectors.toList());
		
		int successCount = 0;
		int failCount = 0;
		
		for (CompletableFuture<Boolean> result : results) {
			if(result.get()) {
				successCount++;
			} else {
				failCount++;
			}
		}
		
		Mockito.verify(soloApiClient, Mockito.times(expected.invocations)).createInvoice(Mockito.any());
		assertThat("Expected two succesful calls", successCount, equalTo(expected.succeses));
		assertThat("Expected one failed calls", failCount, equalTo(expected.errors));
	}
	
	@Test
	public void postOrder_SimulateShopifyFiveErrors() throws Exception {
		delayCreateReceipt(1000);
		simulateShopify(new ShopifyTestSpec().errors(5).timeBetweenCalls(1000).invocationTime(5000));
	}
	
	private void simulateShopify(ShopifyTestSpec testSpec) throws Exception {
		
		delayCreateReceipt(testSpec.invocationTime);
		
		List<ShopifyOrder> orders = new ArrayList<>();
		for (int i = 0; i<20; i++) {
			orders.add(ShopifyOrderCreator.createOrder("1"));
		}
		RuntimeException [] exceptions = new RuntimeException[testSpec.errors];
		for (int i = 0; i<testSpec.errors; i++) {
			exceptions[i] = new RuntimeException();
		}
		if (testSpec.errors > 0) {
			Mockito.when(soloApiClient.createInvoice(Mockito.any()))
			.thenThrow(exceptions)
			.thenReturn(null);
		} else {
			Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenReturn(null);
		}
		for(ShopifyOrder order: orders) {
			CompletableFuture<Boolean> result =  postOrderAsync(order, null);
			if (testSpec.timeBetweenCalls > 0) {
				Thread.sleep(testSpec.timeBetweenCalls);
				result.complete(false);
				if (result.get()) {
					break;
				}
			}
		}
	
		Mockito.verify(soloApiClient, Mockito.times(testSpec.errors+1)).createInvoice(Mockito.any());
	}
	
	private static class ShopifyTestSpec {
		int timeBetweenCalls;
		int invocationTime;
		int errors;
		
		public ShopifyTestSpec timeBetweenCalls(int timeBetweenCalls) {
			this.timeBetweenCalls = timeBetweenCalls;
			return this;
		}
		public ShopifyTestSpec invocationTime(int invocationTime) {
			this.invocationTime = invocationTime;
			return this;
		}
		public ShopifyTestSpec errors(int errors) {
			this.errors = errors;
			return this;
		}
		

		
		
	}
	
	
	private static class Expectations {
		int invocations;
		int succeses;
		int errors;
		
		public Expectations invocations(int expectedInvocations) {
			this.invocations = expectedInvocations;
			return this;
		}
		public Expectations successes(int expectedSuccesses) {
			this.succeses = expectedSuccesses;
			return this;
		}
		public Expectations errors(int expectedErrors) {
			this.errors = expectedErrors;
			return this;
		}
		
		
	}

	private void delayCreateReceipt(long sleepTime) {
		Mockito.doAnswer((Answer<?>) invocation -> {
	        Thread.sleep(sleepTime);
	        return null;
	    }).when(soloApiClient).createInvoice(Mockito.any());
	}
	
	
	private CompletableFuture<Boolean> postOrderAsync(ShopifyOrder order, ContentCachingRequestWrapper request) {
		CompletableFuture<Boolean> result = CompletableFuture.supplyAsync(() -> {
			Boolean success = true;
			try {
				orderController.postOrder(order, null);
			} catch (Exception e) {
				success = false;
			}
			return success;
		});
		return result;
	}
	
	

}
