package org.bytepoet.shopifysolo.manager.controllers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceDetailsFiscalized;
import static org.bytepoet.shopifysolo.webinvoice.models.WebInvoiceModels.webInvoiceResponseFiscalized;
import java.util.Calendar;
import java.util.Date;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.controllers.OrderController;
import org.bytepoet.shopifysolo.controllers.ShopifyOrderCreator;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderArchive;
import org.bytepoet.shopifysolo.manager.repositories.OrderArchiveRepository;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class OrderArchiveControllerTest {
	
	@MockBean
	private WebInvoiceClient webInvoiceClient;
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@MockBean
	private MailService mailService;
	
	@Autowired
	private OrderController orderController;
	
	@Autowired
	private OrderManagerController orderManagerController;
	
	@Autowired
	private OrderArchiveController orderArchiveController;
	
	@Autowired
	private OrderArchiveRepository archiveRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Before
	public void before() {
		archiveRepository.deleteAll();
		orderRepository.deleteAll();
	}
	
	@Test
	public void archiveOrdersWithDates_success() throws Exception {
		provideValidOrders("1", "2", "3", "4", "5");
		Calendar calendar = Calendar.getInstance();
		calendar.set(2019, 0, 1);
		Date start = calendar.getTime();
		calendar.set(2020, 11, 31);
		Date end = calendar.getTime();
		
		orderArchiveController.addToArchive(start, end);
		
		OrderArchive result = orderArchiveController.viewArchive();
		
		Assert.assertThat(result.getOrders().size(), equalTo(5));
		Page<Order> page = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, null, 0, 20, null, null);
		Assert.assertThat(page.getTotalElements(), equalTo(0L));
		
	}
	
	@Test
	public void updateArchive() throws Exception {
		provideValidOrders("1", "2", "3", "4", "5");
		Calendar calendar = Calendar.getInstance();
		calendar.set(2019, 0, 1);
		Date start = calendar.getTime();
		calendar.set(2020, 11, 31);
		Date end = calendar.getTime();
		
		orderArchiveController.addToArchive(start, end);
		
		provideValidOrders("6", "7", "8", "9", "10");
		orderArchiveController.addToArchive(start, end);
		OrderArchive result = orderArchiveController.viewArchive();
		
		Assert.assertThat(result.getOrders().size(), equalTo(10));
		Page<Order> page = orderManagerController.getOrders(null, null, null, null, null, null, null, null, null, null, 0, 20, null, null);
		Assert.assertThat(page.getTotalElements(), equalTo(0L));
		
	}
	
	private void provideValidOrders(String... ids) throws Exception {
		for (String id: ids) {
			Mockito.doReturn(webInvoiceResponseFiscalized()).when(webInvoiceClient).createInvoice(Mockito.any(), Mockito.any());
			Mockito.doReturn(webInvoiceDetailsFiscalized()).when(webInvoiceClient).getInvoiceDetails(Mockito.any(), Mockito.any());
			ShopifyOrder order = ShopifyOrderCreator.createOrder(id);
			orderController.postOrder(order, null);
		}
		
	}
}
