package org.bytepoet.shopifysolo.manager.controllers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Collections;

import javax.transaction.Transactional;

import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceDetailsFiscalized;
import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceResponseFiscalized;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.controllers.OrderController;
import org.bytepoet.shopifysolo.controllers.ShopifyOrderCreator;
import org.bytepoet.shopifysolo.controllers.TenderController;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.manager.repositories.RefundRepository;
import org.bytepoet.shopifysolo.manager.utils.RefundTransactionalService;
import org.bytepoet.shopifysolo.services.MailService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyCreateOrder;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.webinvoice.client.WebInvoiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;


@SpringBootTest
@AutoConfigureEmbeddedDatabase
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class OrderManagerControllerTests {

	@MockBean
	private WebInvoiceClient webInvoiceClient;
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@MockBean
	private MailService mailService;
	
	@Autowired
	private OrderController orderController;
	
	@Autowired
	private TenderController tenderController;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired 
	private RefundRepository refundRepository;
	
	@Autowired
	private RefundTransactionalService refundTransactionalService;
	
	@Autowired
	private OrderManagerController orderManagerController;
	
	
	
	
	@Before
	public void before() {
		refundTransactionalService.deleteAll();
		orderRepository.deleteAll();
	}
	
	@Test
	public void getOrders_orderSentByShopify_oneOrderReturned() throws Exception {
		provideValidOrders("1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null,null, null, null, null, null, null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		assertValidOrder(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoSameOrdersSentByShopify_oneOrderReturned() throws Exception {
		provideValidOrders("1", "1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null,null, null, null, null, null, null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		Assert.assertThat(orderPage.getContent().get(0), instanceOf(PaymentOrder.class));
		assertValidOrder(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoDifferentOrdersSentByShopify_twoOrdersReturned() throws Exception {
		provideValidOrders("1", "2");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 2, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(2L));
		
		assertValidOrder(orderPage.getContent().get(0), "1");
		assertValidOrder(orderPage.getContent().get(1), "2");
	}
	
	
	@Test
	public void getOrders_tenderSentByShopify_oneOrderReturned() throws Exception {
		provideValidTenders("1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null,  null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		assertValidUnpaidTender(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoSameTendersSentByShopify_oneOrderReturned() throws Exception {
		provideValidTenders("1", "1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null,null, null, null, null, null, null, 0, 1, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(1L));
		assertValidUnpaidTender(orderPage.getContent().get(0), "1");
	}
	
	@Test
	public void getOrders_twoDifferentTendersSentByShopify_twoOrdersReturned() throws Exception {
		provideValidTenders("1", "2");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 2, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(2L));
		
		assertValidUnpaidTender(orderPage.getContent().get(0), "1");
		assertValidUnpaidTender(orderPage.getContent().get(1), "2");
	}
	
	
	@Test
	public void getOrders_twoTendersAndTwoOrders_fourOrdersReturned() throws Exception {
		provideValidTenders("1", "2");
		provideValidOrders("3", "4");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 4, null, null);
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
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 2, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(2L));
		
		assertValidTenderOrder(orderPage.getContent().get(0), "1");
		assertValidTenderOrder(orderPage.getContent().get(1), "2");
	}
	
	@Test
	public void getOrders_twoTendersOnePaidOneOrder_twoOrdersReturned() throws Exception {
		provideValidTenders("1", "2");
		provideValidOrders("1", "3");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 3, null, null);
		Assert.assertThat(orderPage.getTotalElements(), equalTo(3L));
		
		assertValidTenderOrder(orderPage.getContent().get(0), "1");
		assertValidUnpaidTender(orderPage.getContent().get(1), "2");
		assertValidOrder(orderPage.getContent().get(2), "3");
	}
	
	@Test
	public void refundOrder_ok() throws Exception {
		provideValidOrders("1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 1, null, null);
		PaymentOrder order = (PaymentOrder) orderPage.getContent().get(0);
		
		Item item = order.getItems().get(0);
		orderManagerController.refundOrder(order.getId(), Collections.singletonList(item.getId()));
		Refund refund = refundTransactionalService.findAll().get(0);
		Assert.assertThat(refund.getItems().get(0).isRefunded(), equalTo(true));
		Assert.assertThat(refund.getItems().get(0).getRefundId(), equalTo(refund.getId()));
		Assert.assertThat(refund.getTotalPrice(), equalTo(229.0));
		
		PaymentOrder afterRefundOrder = (PaymentOrder) orderManagerController.getOrder(order.getId());
		Assert.assertThat(afterRefundOrder.getItems().size(), equalTo(order.getItems().size()));
		Assert.assertThat(afterRefundOrder.getTotalPrice(), equalTo(order.getTotalPrice()));
	}
	
	@Test
	public void refundOrderCustom_ok() throws Exception {
		provideValidOrders("1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 1, null, null);
		PaymentOrder order = (PaymentOrder) orderPage.getContent().get(0);
		
		Item item = new Item("Custom refund", "100", 1, "0", "25");
				
		orderManagerController.refundOrderCustom(order.getId(), Collections.singletonList(item));
		Refund refund = refundTransactionalService.findAll().get(0);
		Assert.assertThat(refund.getItems().get(0).isRefunded(), equalTo(true));
		Assert.assertThat(refund.getItems().get(0).getRefundId(), equalTo(refund.getId()));
		Assert.assertThat(refund.getTotalPrice(), equalTo(100.0));
		
		PaymentOrder afterRefundOrder = (PaymentOrder) orderManagerController.getOrder(order.getId());
		Assert.assertThat(afterRefundOrder.getItems().size(), equalTo(order.getItems().size()));
		Assert.assertThat(afterRefundOrder.getTotalPrice(), equalTo(order.getTotalPrice()));
		
	}
	
	@Test
	public void refundOrder_thenUpdateOrder_ok() throws Exception {
		provideValidOrders("1");
		Page<Order> orderPage = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, 0, 1, null, null);
		PaymentOrder order = (PaymentOrder) orderPage.getContent().get(0);
		
		Item item = order.getItems().get(0);
		orderManagerController.refundOrder(order.getId(), Collections.singletonList(item.getId()));
		
		PaymentOrder afterRefundOrder = (PaymentOrder) orderManagerController.getOrder(order.getId());
		ObjectMapper mapper = new ObjectMapper();
		String orderJson = mapper.writeValueAsString(afterRefundOrder);
		orderJson = orderJson.replace("INITIAL", "IN_PROCESS");
		Order orderToUpdate = mapper.readValue(orderJson, Order.class);
		Assert.assertThat(orderToUpdate.getStatus(), equalTo(OrderStatus.IN_PROCESS));
		
		orderManagerController.save(orderToUpdate);
		
		
		Refund refund = refundTransactionalService.findAll().get(0);
		Assert.assertThat(refund.getItems().get(0).isRefunded(), equalTo(true));
		Assert.assertThat(refund.getItems().get(0).getRefundId(), equalTo(refund.getId()));
		Assert.assertThat(refund.getTotalPrice(), equalTo(229.0));
		
		PaymentOrder refreshedOrder = (PaymentOrder) orderManagerController.getOrder(order.getId());
		Assert.assertThat(refreshedOrder.getItems().size(), equalTo(order.getItems().size()));
		Assert.assertThat(refreshedOrder.getTotalPrice(), equalTo(order.getTotalPrice()));
		Assert.assertThat(refreshedOrder.getStatus(), equalTo(OrderStatus.IN_PROCESS));
		
	}
	
	@Test
	public void createGiveaway_thenOrder_ok() {

	}
	
	@Test
	public void createGiveaway_thenTender_ok() {
		
	}
	
	
	private void provideValidOrders(String... ids) throws Exception {
		for (String id: ids) {
			Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
			Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
			ShopifyOrder order = ShopifyOrderCreator.createOrder(id);
			orderController.postOrder(order, null);
		}
		
	}
	
	private void provideValidTenders(String... ids) throws Exception {
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
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(false));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(false));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(false));
	}
	
	private void assertValidTenderOrder(Order order, String id) {
		Assert.assertThat(order, instanceOf(PaymentOrder.class));
		PaymentOrder paymentOrder = (PaymentOrder) order;
		Assert.assertThat(paymentOrder.isReceiptCreated(), equalTo(true));
		Assert.assertThat(paymentOrder.isReceiptSent(), equalTo(true));
		Assert.assertThat(paymentOrder.isPaid(), equalTo(true));
	}
	
}
