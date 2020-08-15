package org.bytepoet.shopifysolo.controllers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceDetailsFiscalized;
import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceDetailsNotFiscalized;
import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceResponseFiscalized;
import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceResponseNotFiscalized;

import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.MailService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.webinvoice.client.WebInvoiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;



@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class OrderControllerTests {

	@MockBean
	private WebInvoiceClient webInvoiceClient;
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@MockBean
	private MailService mailService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@LocalServerPort
	private int serverPort;

	@Before
	public void setup() {
		CachedFunctionalService.clearCache();
		orderRepository.deleteAll();
	    RestAssured.port = serverPort;
		
	}
	
	@Test
	public void postOrder_OneValidOrder_Ok() throws Exception {
		
		Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		ShopifyOrder order = ShopifyOrderCreator.createOrder("1");
		postOrder(order);
		Mockito.verify(webInvoiceClient, Mockito.times(1)).createInvoice(Mockito.any(), Mockito.any());
		assertValidOrder("1");
	}
	
	@Test
	public void postOrder_ValidTwoDifferentOrders_Ok() throws Exception {
		Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("2");
		postOrder(order1);
		Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		postOrder(order2);
		Mockito.verify(webInvoiceClient, Mockito.times(2)).createInvoice(Mockito.any(), Mockito.any());
		assertValidOrder("1");
		assertValidOrder("2");
		
	}
	
	@Test
	public void postOrder_ValidTwoSameOrders_Ok() throws Exception {
		Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		postOrder(order1);
		postOrder(order2);
		Mockito.verify(webInvoiceClient, Mockito.times(1)).createInvoice(Mockito.any(), Mockito.any());
		assertValidOrder("1");
		
	}
	
	@Test
	public void postOrder_ThreeSameOrdersFirstFails_SecondExecutedThirdSkipped() throws Exception {	
		Mockito.doThrow(new RuntimeException()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		postOrder(order1);
		
		Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order3 = ShopifyOrderCreator.createOrder("1");
		postOrder(order2);
		postOrder(order3);
		Mockito.verify(webInvoiceClient, Mockito.times(2)).createInvoice(Mockito.any(), Mockito.any());
		assertValidOrder("1");
	}
	
	@Test
	public void postOrder_ThreeSameOrdersFirstDetailsFails_SecondExecutedThirdSkipped() throws Exception {
		Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		Mockito.doThrow(new RuntimeException()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		
		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		postOrder(order1);
		
		Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		ShopifyOrder order2 = ShopifyOrderCreator.createOrder("1");
		ShopifyOrder order3 = ShopifyOrderCreator.createOrder("1");
		postOrder(order2);
		postOrder(order3);
		Mockito.verify(webInvoiceClient, Mockito.times(1)).createInvoice(Mockito.any(), Mockito.any());
		assertValidOrder("1");
	}
	
	
	@Test
	public void postOrder_FiscalizationFails_SuccessOnRetry() throws Exception {	
		Mockito.doReturn(webInvoiceResponseNotFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
		Mockito.doReturn(webInvoiceDetailsNotFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		
		ShopifyOrder order1 = ShopifyOrderCreator.createOrder("1");
		Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
		postOrder(order1);
		Mockito.verify(webInvoiceClient, Mockito.times(1)).createInvoice(Mockito.any(), Mockito.any());
		assertValidOrder("1");
	}
	
	private Response postOrder(ShopifyOrder order1) throws Exception {
		return RestAssured.given()
			.contentType(ContentType.JSON)
			.body(order1)
			.post("/orders");
		//orderController.postOrder(order1, null);
	}
	
	private void assertValidOrder(String id) {
		PaymentOrder paymentOrder = orderRepository.getOrderWithShopifyId(id).get();
		Assert.assertThat(paymentOrder.getInvoiceId(), notNullValue());
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(true));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(true));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(true));
	}


	
	

}
