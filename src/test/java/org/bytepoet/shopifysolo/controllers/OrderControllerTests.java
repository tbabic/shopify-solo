package org.bytepoet.shopifysolo.controllers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.ContentCachingRequestWrapper;



@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class OrderControllerTests {

	@MockBean
	private SoloApiClient soloApiClient;
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@MockBean
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private OrderController orderController;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Before
	public void setup() {
		CachedFunctionalService.clearCache();
		orderRepository.deleteAll();
		
	}
	
	@Test
	public void postOrder_OneValidOrder_Ok() throws Exception {
		Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenAnswer(soloInvoiceAnswer());
		ShopifyOrder order = ShopifyOrderCreator.createOrder("1");
		orderController.postOrder(order, null);
		Mockito.verify(soloApiClient).createInvoice(Mockito.any());
		assertValidOrder("1");
	}
	
	@Test
	public void postOrder_ValidTwoDifferentOrders_Ok() throws Exception {
		Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenAnswer(soloInvoiceAnswer());
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("2");
		orderController.postOrder(order1, null);
		orderController.postOrder(order2, null);
		Mockito.verify(soloApiClient, Mockito.times(2)).createInvoice(Mockito.any());
		assertValidOrder("1");
		
	}
	
	@Test
	public void postOrder_ValidTwoSameOrders_Ok() throws Exception {
		Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenAnswer(soloInvoiceAnswer());
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		orderController.postOrder(order1, null);
		orderController.postOrder(order2, null);
		Mockito.verify(soloApiClient, Mockito.times(1)).createInvoice(Mockito.any());
		assertValidOrder("1");
		
	}
	
	@Test
	public void postOrder_ThreeSameOrdersFirstFails_SecondExecutedThirdSkipped() throws Exception {		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order3 = ShopifyOrderCreator.createOrder("1");
		Mockito.when(soloApiClient.createInvoice(Mockito.any()))
			.thenThrow(RuntimeException.class)
			.thenAnswer(soloInvoiceAnswer());
		try {
			orderController.postOrder(order1, null);
			Assert.fail("Expected runtime exception");
		} catch(RuntimeException e) {
			//expected
		}
		orderController.postOrder(order2, null);
		orderController.postOrder(order3, null);
		Mockito.verify(soloApiClient, Mockito.times(2)).createInvoice(Mockito.any());
		assertValidOrder("1");
	}

	
	@Test
	public void postOrder_TwoOrdersFirstFailsPartiallyCacheClearedBeforeSecond_Ok() throws Exception {		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		Mockito.when(soloApiClient.createInvoice(Mockito.any()))
			.thenAnswer(soloInvoiceAnswer());
		Mockito.doThrow(new RuntimeException()).doNothing().when(soloMaillingService)
			.sendEmailWithPdf(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		orderController.postOrder(order1, null);
		
		CachedFunctionalService.clearCache();
		
		orderController.postOrder(order2, null);
		Mockito.verify(soloApiClient, Mockito.times(1)).createInvoice(Mockito.any());
		assertValidOrder("1");
		
	}
	
	
	@Test
	public void postOrder_ValidTwoDifferentAsyncOrders_Ok() throws Exception {
		Mockito.when(soloApiClient.createInvoice(Mockito.any()))
			.thenAnswer(soloInvoiceAnswer(new AnswerSpec().invocationTime(1000)));
		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("2");
		CompletableFuture<Boolean> result1 = postOrderAsync(order1, null);
		CompletableFuture<Boolean> result2 = postOrderAsync(order2, null);
		assertThat(result1.get(), equalTo(true));
		assertThat(result2.get(), equalTo(true));
		Mockito.verify(soloApiClient, Mockito.times(2)).createInvoice(Mockito.any());
		assertValidOrder("1");
		
	}
	
	@Test
	public void postOrder_ValidTwoSameAsyncOrders_Ok() throws Exception {
		
		executeMultipleAsyncOrders(2, 0, 1000, new Expectations().invocations(1).errors(0).successes(2));
	}
	
	
	@Test
	public void postOrder_TwoSameAsyncOrdersFirstFails_SecondExecuted() throws Exception {
		executeMultipleAsyncOrders(2, 1, 1000, new Expectations().invocations(2).errors(1).successes(1));
	}
	
	@Test
	public void postOrder_ThreeSameAsyncOrdersFirstFails_SecondExecutedThirdSkipped() throws Exception {
		executeMultipleAsyncOrders(3, 1, 1000, new Expectations().invocations(2).errors(1).successes(2));
	}
	
	@Test
	public void postOrder_TenSameAsyncOrdersFirstFiveFails_SecondExecutedThirdSkipped() throws Exception {
		executeMultipleAsyncOrders(10, 5, 1000, new Expectations().invocations(6).errors(5).successes(5));
	}
	
	@Test
	public void postOrder_SimulateShopifyFiveErrors() throws Exception {
		simulateShopify(new ShopifyTestSpec().errors(5).timeBetweenCalls(1000).invocationTime(5000));
	}
	
	private void assertValidOrder(String id) {
		PaymentOrder paymentOrder = orderRepository.getOrderWithShopifyId(id).get();
		Assert.assertThat(paymentOrder.getInvoiceId(), notNullValue());
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(true));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(true));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(true));
	}
	
	private static class AnswerSpec {
		
		private int successiveErrors = 0;
		private long invocationTime = 0;
		
		public AnswerSpec successiveErrors(int successiveErrors) {
			this.successiveErrors = successiveErrors;
			return this;
		}
		public AnswerSpec invocationTime(long invocationTime) {
			this.invocationTime = invocationTime;
			return this;
		}
		
		
	}
	
	
	
	
	
	private void executeMultipleAsyncOrders(int createdOrders, int successiveErrors, long invocationTime, Expectations expected) throws Exception {
		
		List<ShopifyOrder> orders = new ArrayList<>();
		for (int i = 0; i<createdOrders; i++) {
			orders.add(ShopifyOrderCreator.createOrder("1"));
		}
		Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenAnswer(
				soloInvoiceAnswer(new AnswerSpec().invocationTime(invocationTime).successiveErrors(successiveErrors)));

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
		assertThat("Expected succesful calls", successCount, equalTo(expected.succeses));
		assertThat("Expected failed calls", failCount, equalTo(expected.errors));
		assertValidOrder("1");
	}
	
	
	
	private void simulateShopify(ShopifyTestSpec testSpec) throws Exception {
		
		List<ShopifyOrder> orders = new ArrayList<>();
		for (int i = 0; i<20; i++) {
			orders.add(ShopifyOrderCreator.createOrder("1"));
		}
		Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenAnswer(
				soloInvoiceAnswer(new AnswerSpec().invocationTime(testSpec.invocationTime).successiveErrors(testSpec.errors)));
		List<CompletableFuture<Boolean>> results = new ArrayList<>();
		for(ShopifyOrder order: orders) {
			CompletableFuture<Boolean> result =  postOrderAsync(order, null);
			if (testSpec.timeBetweenCalls > 0) {
				Thread.sleep(testSpec.timeBetweenCalls);
				results.add(result);
				if (result.isDone()) {
					if (result.get()) {
						break;
					}
				}
			}
		}
		int failures = 0;
		int successes = 0;
		int executions = results.size();
		for (CompletableFuture<Boolean> result: results) {
			if (result.get()) {
				successes++;
			} else {
				failures++;
			}
		}
		Assert.assertThat(failures, equalTo(testSpec.errors));
		Assert.assertThat(successes, equalTo(executions-testSpec.errors));
		Mockito.verify(soloApiClient, Mockito.times(testSpec.errors+1)).createInvoice(Mockito.any());
		assertValidOrder("1");
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
	
	private Answer<SoloInvoice> soloInvoiceAnswer() {
		return soloInvoiceAnswer(new AnswerSpec().successiveErrors(0).invocationTime(0));
	}
	
	private Answer<SoloInvoice> soloInvoiceAnswer(AnswerSpec answerSpec) {
		return new Answer<SoloInvoice>() {
			
			private int executedErrors = 0;
			
			@Override
			public SoloInvoice answer(InvocationOnMock invocation) throws Throwable {
				synchronized(this) {
					if (answerSpec.invocationTime > 0) {
						Thread.sleep(answerSpec.invocationTime);
					}
					if (executedErrors < answerSpec.successiveErrors) {
						executedErrors++;
						throw new RuntimeException();
					}
					SoloInvoice invoice = invocation.getArgument(0);
					Field field = SoloBillingObject.class.getDeclaredField("id");
					field.setAccessible(true);
					field.set(invoice, RandomStringUtils.randomAlphanumeric(10));
					
					return invoice;
				}
			}
		};
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
