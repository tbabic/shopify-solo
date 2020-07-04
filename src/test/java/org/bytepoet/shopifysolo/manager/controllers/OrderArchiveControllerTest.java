package org.bytepoet.shopifysolo.manager.controllers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.controllers.OrderController;
import org.bytepoet.shopifysolo.controllers.ShopifyOrderCreator;
import org.bytepoet.shopifysolo.controllers.TenderController;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderArchive;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
import org.junit.Assert;
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
public class OrderArchiveControllerTest {

	@MockBean
	private SoloApiClient soloApiClient;
	
	@MockBean
	private AuthorizationService authorizationService;
	
	@MockBean
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private OrderController orderController;
	
	@Autowired
	private OrderManagerController orderManagerController;
	
	@Autowired
	private TenderController tenderController;
	
	@Autowired
	private OrderArchiveController orderArchiveController;
	
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
		Page<Order> page = orderManagerController.getOrders(null, null, null, null, null, null, null, null, 0, 20, null, null);
		Assert.assertThat(page.getTotalElements(), equalTo(0L));
		
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
