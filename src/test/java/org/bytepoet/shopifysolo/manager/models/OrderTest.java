package org.bytepoet.shopifysolo.manager.models;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class OrderTest {

	@Test
	public void test_serializePaymentOrder() throws Exception {
		Order order = new PaymentOrder();
		ObjectMapper mapper = new ObjectMapper();
		String orderString = mapper.writeValueAsString(order);
		Assert.assertThat(orderString, containsString("type"));
		Order deserialized = mapper.readValue(orderString, Order.class);
		Assert.assertThat(deserialized, instanceOf(PaymentOrder.class));
	}
	
	@Test
	public void test_serializeGiveawayOrder() throws Exception {
		Order order = new GiveawayOrder();
		ObjectMapper mapper = new ObjectMapper();
		String orderString = mapper.writeValueAsString(order);
		Assert.assertThat(orderString, containsString("type"));
		Order deserialized = mapper.readValue(orderString, Order.class);
		Assert.assertThat(deserialized, instanceOf(GiveawayOrder.class));
	}
	
	@Test
	public void test_serializePaymentOrdersList() throws Exception {
		Order order = new PaymentOrder();
		List<Order> orderList = Arrays.asList(order);
		ObjectMapper mapper = new ObjectMapper();
		String orderString = mapper.writeValueAsString(orderList);
		Assert.assertThat(orderString, containsString("type"));
	}
	
	@Test
	public void test_serializePaymentOrdersPage() throws Exception {
		Order order = new PaymentOrder();
		Page<Order> orderPage = new PageImpl<>(Arrays.asList(order));
		ObjectMapper mapper = new ObjectMapper();
		String orderString = mapper.writeValueAsString(orderPage);
		Assert.assertThat(orderString, containsString("type"));
	}
	
}
