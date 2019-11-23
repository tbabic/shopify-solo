package org.bytepoet.shopifysolo.feature;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.MessageFormat;
import java.util.List;

import org.bytepoet.shopifysolo.manager.database.DatabaseTable;
import org.bytepoet.shopifysolo.manager.database.MockGoogleSheetsService;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.GoogleSheetsService;
import org.bytepoet.shopifysolo.services.SoloMaillingService;
import org.bytepoet.shopifysolo.solo.clients.MockSoloApiClient;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PaidWebShopOrderTests {
	
	@LocalServerPort
    private int port;
	
	@MockBean
	private SoloMaillingService soloMaillingService;
	
	@Autowired
	private MockGoogleSheetsService googleSheetsService;
	
	@Autowired
	private OrderRepository orderRepository;

	@org.springframework.boot.test.context.TestConfiguration
	@Profile("test")
	public static class TestConfiguration {
		
		@Bean
		@Primary
		public GoogleSheetsService googleSheetsService() {
			return new MockGoogleSheetsService();
		}
		
		@Bean
		@Primary
		public SoloApiClient soloApiClient() {
			return new MockSoloApiClient();
		}
	}
	
	@Before
	public void before() throws Exception {
		CachedFunctionalService.clearCache();
		googleSheetsService.clear();
	}
	
	@Test
	public void createWebshopOrder_OneOrder_OneResult() throws InterruptedException {
		
		String json = ResourceLoader.getShopifyCorvusOrderJson();
		RestAssured.given().contentType(ContentType.JSON).when().body(json)
				.post(getLocalUrl("/orders")).then()
				.assertThat().statusCode(200);
		
		Thread.sleep(1000);
		List<Order> orders = RestAssured.given().when().get(getLocalUrl("/manager/orders")).then()
				.extract().body().as(new TypeRef<List<Order>>() {});
		assertThat(orders.size(), equalTo(1));
		assertThat(orders.get(0), is(instanceOf(PaymentOrder.class)));
		assertThat(((PaymentOrder)orders.get(0)).getEmail(), equalTo("kfulir@gmail.com"));
		
		verify(soloMaillingService, times(1)).sendEmailWithPdf(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}
	
	@Test
	public void createWebshopOrder_TwoSameOrders_OneResult() throws InterruptedException {
		
		String json = ResourceLoader.getShopifyCorvusOrderJson();
		RestAssured.given().contentType(ContentType.JSON).when().body(json)
				.post(getLocalUrl("/orders")).then()
				.assertThat().statusCode(200);
		RestAssured.given().contentType(ContentType.JSON).when().body(json)
				.post(getLocalUrl("/orders")).then()
				.assertThat().statusCode(200);
		
		Thread.sleep(1000);
		List<Order> orders = RestAssured.given().when().get(getLocalUrl("/manager/orders")).then()
				.extract().body().as(new TypeRef<List<Order>>() {});
		assertThat(orders.size(), equalTo(1));
		assertThat(orders.get(0), is(instanceOf(PaymentOrder.class)));
		assertThat(((PaymentOrder)orders.get(0)).getEmail(), equalTo("kfulir@gmail.com"));
		
		verify(soloMaillingService, times(1)).sendEmailWithPdf(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		
	}
	
	@Test
	public void createWebshopOrder_TwoSameOrdersCacheCleared_OneResult() throws InterruptedException {
		
		String json = ResourceLoader.getShopifyCorvusOrderJson();
		RestAssured.given().contentType(ContentType.JSON).when().body(json)
				.post(getLocalUrl("/orders")).then()
				.assertThat().statusCode(200);
		
		CachedFunctionalService.clearCache();
		
		RestAssured.given().contentType(ContentType.JSON).when().body(json)
				.post(getLocalUrl("/orders")).then()
				.assertThat().statusCode(200);
		
		Thread.sleep(1000);
		List<Order> orders = RestAssured.given().when().get(getLocalUrl("/manager/orders")).then()
				.extract().body().as(new TypeRef<List<Order>>() {});
		
		assertThat(orders.size(), equalTo(1));
		assertThat(orders.get(0), is(instanceOf(PaymentOrder.class)));
		assertThat(((PaymentOrder)orders.get(0)).getEmail(), equalTo("kfulir@gmail.com"));
		
		verify(soloMaillingService, times(1)).sendEmailWithPdf(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		
	}
	
	
	
	private String getLocalUrl(String path) {
		return MessageFormat.format("http://localhost:{0}{1}", String.valueOf(this.port), path);
	}


}
