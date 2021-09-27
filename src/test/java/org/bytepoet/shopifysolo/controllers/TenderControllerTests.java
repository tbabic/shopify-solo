package org.bytepoet.shopifysolo.controllers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class TenderControllerTests {
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@Autowired
	private TenderController tenderController;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Before
	public void setup() {
		CachedFunctionalService.clearCache();
		orderRepository.deleteAll();
		
	}
	
	@Test
	public void postOrder_PaymentOrder_NotProcessed() throws Exception {
		ShopifyOrder order = ShopifyOrderCreator.createOrder("1");
		tenderController.postOrder(order, null);
		Assert.assertThat(orderRepository.count(), equalTo(0L));
	}
	
	@Test
	public void postOrder_OneValidOrder_Ok() throws Exception {
		ShopifyOrder order = ShopifyOrderCreator.createTender("1");
		tenderController.postOrder(order, null);
		assertValidOrder("1");
	}
	
	@Test
	public void postOrder_ValidTwoDifferentOrders_Ok() throws Exception {
		ShopifyOrder order1 = ShopifyOrderCreator.createTender("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createTender("2");
		tenderController.postOrder(order1, null);
		tenderController.postOrder(order2, null);
		assertValidOrder("1");
		assertValidOrder("2");
		
	}
	
	@Test
	public void postOrder_ValidTwoSameOrders_Ok() throws Exception {
		ShopifyOrder order1 = ShopifyOrderCreator.createTender("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createTender("1");
		tenderController.postOrder(order1, null);
		tenderController.postOrder(order2, null);
		assertValidOrder("1");
		
	}
	
	
	private void assertValidOrder(String id) {
		PaymentOrder paymentOrder = orderRepository.getPaymentOrderWithShopifyId(id).get();
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(false));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(false));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(false));
	}

}
