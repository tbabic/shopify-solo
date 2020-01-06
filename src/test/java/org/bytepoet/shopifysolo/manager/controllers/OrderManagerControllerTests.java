package org.bytepoet.shopifysolo.manager.controllers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.lang.reflect.Field;

import org.apache.commons.lang3.RandomStringUtils;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.controllers.OrderController;
import org.bytepoet.shopifysolo.controllers.ShopifyOrderCreator;
import org.bytepoet.shopifysolo.controllers.TenderController;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
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
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class OrderManagerControllerTests {

	@MockBean
	private SoloApiClient soloApiClient;
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@MockBean
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private OrderController orderController;
	
	@Autowired
	private TenderController tenderController;
	
	@Autowired
	private OrderRepository orderRepository;
	
	
	@Autowired
	private OrderManagerController orderManagerController;
	
	@Before
	public void before() {
		orderRepository.deleteAll();
	}
	
	@Test
	public void getOrders_orderSentByShopify_oneOrderReturned() throws Exception {
		provideValidOrders("1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null,null, null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		assertValidOrder(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoSameOrdersSentByShopify_oneOrderReturned() throws Exception {
		provideValidOrders("1", "1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null,null, null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		Assert.assertThat(orderPage.getContent().get(0), instanceOf(PaymentOrder.class));
		assertValidOrder(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoDifferentOrdersSentByShopify_twoOrdersReturned() throws Exception {
		provideValidOrders("1", "2");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, 0, 2, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(2L));
		
		assertValidOrder(orderPage.getContent().get(0), "1");
		assertValidOrder(orderPage.getContent().get(1), "2");
	}
	
	
	@Test
	public void getOrders_tenderSentByShopify_oneOrderReturned() throws Exception {
		provideValidTenders("1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		assertValidUnpaidTender(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoSameTendersSentByShopify_oneOrderReturned() throws Exception {
		provideValidTenders("1", "1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		assertValidUnpaidTender(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoDifferentTendersSentByShopify_twoOrdersReturned() throws Exception {
		provideValidTenders("1", "2");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, 0, 2, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(2L));
		
		assertValidUnpaidTender(orderPage.getContent().get(0), "1");
		assertValidUnpaidTender(orderPage.getContent().get(1), "2");
	}
	
	
	@Test
	public void getOrders_twoTendersAndTwoOrders_fourOrdersReturned() throws Exception {
		provideValidTenders("1", "2");
		provideValidOrders("3", "4");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, 0, 4, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(4L));
		
		assertValidUnpaidTender(orderPage.getContent().get(0), "1");
		assertValidUnpaidTender(orderPage.getContent().get(1), "2");
		assertValidOrder(orderPage.getContent().get(2), "3");
		assertValidOrder(orderPage.getContent().get(3), "4");
	}
	
	@Test
	public void getOrders_twoTendersAndPaid_twoOrdersReturned() throws Exception {
		provideValidTenders("1", "2");
		provideValidOrders("1", "2");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, 0, 2, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(2L));
		
		assertValidTenderOrder(orderPage.getContent().get(0), "1");
		assertValidTenderOrder(orderPage.getContent().get(1), "2");
	}
	
	@Test
	public void getOrders_twoTendersOnePaidOneOrder_twoOrdersReturned() throws Exception {
		provideValidTenders("1", "2");
		provideValidOrders("1", "3");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, 0, 3, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(3L));
		
		assertValidTenderOrder(orderPage.getContent().get(0), "1");
		assertValidUnpaidTender(orderPage.getContent().get(1), "2");
		assertValidOrder(orderPage.getContent().get(2), "3");
	}
	
	
	
	
	private void provideValidOrders(String... ids) throws Exception {
		Mockito.when(soloApiClient.createInvoice(Mockito.any())).thenAnswer(soloInvoiceAnswer());
		for (String id: ids) {
			ShopifyOrder order = ShopifyOrderCreator.createOrder(id);
			orderController.postOrder(order, null);
		}
		
	}
	
	private void provideValidTenders(String... ids) throws Exception {
		Mockito.when(soloApiClient.createTender(Mockito.any())).thenAnswer(soloTenderAnswer());
		for (String id: ids) {
			ShopifyOrder order = ShopifyOrderCreator.createTender(id);
			tenderController.postOrder(order, null);
		}
		
	}
	
	private void assertValidOrder(Order order, String id) {
		Assert.assertThat(order, instanceOf(PaymentOrder.class));
		PaymentOrder paymentOrder = (PaymentOrder) order;
		Assert.assertThat(paymentOrder.getInvoiceId(), notNullValue());
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(true));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(true));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(true));
	}
	
	private void assertValidUnpaidTender(Order order, String id) {
		Assert.assertThat(order, instanceOf(PaymentOrder.class));
		PaymentOrder paymentOrder = (PaymentOrder) order;
		Assert.assertThat(paymentOrder.getTenderId(), notNullValue());
		Assert.assertThat(paymentOrder.isTenderCreated(), equalTo(true));
		Assert.assertThat(paymentOrder.isTenderSent(), equalTo(true));
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(false));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(false));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(false));
	}
	
	private void assertValidTenderOrder(Order order, String id) {
		Assert.assertThat(order, instanceOf(PaymentOrder.class));
		PaymentOrder paymentOrder = (PaymentOrder) order;
		Assert.assertThat(paymentOrder.getTenderId(), notNullValue());
		Assert.assertThat(paymentOrder.isTenderCreated(), equalTo(true));
		Assert.assertThat(paymentOrder.isTenderSent(), equalTo(true));
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(true));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(true));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(true));
	}

	
	
	private Answer<SoloInvoice> soloInvoiceAnswer() {
		return new Answer<SoloInvoice>() {
			
			@Override
			public SoloInvoice answer(InvocationOnMock invocation) throws Throwable {
				SoloInvoice invoice = invocation.getArgument(0);
				Field field = SoloBillingObject.class.getDeclaredField("id");
				field.setAccessible(true);
				field.set(invoice, RandomStringUtils.randomAlphanumeric(10));
				
				return invoice;
			}
		};
	}
	
	private Answer<SoloTender> soloTenderAnswer() {
		return new Answer<SoloTender>() {
			
			@Override
			public SoloTender answer(InvocationOnMock invocation) throws Throwable {
				SoloTender tender = invocation.getArgument(0);
				Field field = SoloBillingObject.class.getDeclaredField("id");
				field.setAccessible(true);
				field.set(tender, RandomStringUtils.randomAlphanumeric(10));
				
				return tender;
			}
		};
	}
	
}
