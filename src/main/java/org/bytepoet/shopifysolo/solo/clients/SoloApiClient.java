package org.bytepoet.shopifysolo.solo.clients;

import java.util.List;
import java.util.Map;

import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.bytepoet.shopifysolo.solo.models.SoloPaymentType;
import org.bytepoet.shopifysolo.solo.models.SoloProduct;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



@Service
public class SoloApiClient {
	
	private static final Logger logger = LoggerFactory.getLogger(SoloApiClient.class);
	
	@Value("${solo-api-token}")
	private String apiToken;
	
	@Value("${soloapi.rooturl}")
	private String rootUrl;
	
	@Autowired
	private SoloMapper mapper;
	
	public SoloInvoice createInvoice(SoloInvoice receipt) {
		String endpoint = "/racun";
		MultiValueMap<String, String> parameters = mapper.map(receipt);
		parameters.add("token", apiToken);
		SoloResponse response = executePostRequest(endpoint, parameters);
		return (SoloInvoice) response.getInvoice();
	}
	
	public SoloTender createTender(SoloTender tender) {
		String endpoint = "/ponuda";
		MultiValueMap<String, String> parameters = mapper.map(tender);
		parameters.add("token", apiToken);
		SoloResponse response = executePostRequest(endpoint, parameters);
		return (SoloTender) response.getTender();
	}
	
	public SoloInvoice getInvoice(String invoiceId) {
		String endpoint = "/racun";
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("id", invoiceId);
		parameters.add("token", apiToken);
		SoloResponse response = executeGetRequest(endpoint, parameters);
		return (SoloInvoice) response.getInvoice();
	}
	
	public SoloTender getTender(String tenderId) {
		String endpoint = "/ponuda";
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("id", tenderId);
		parameters.add("token", apiToken);
		SoloResponse response = executeGetRequest(endpoint, parameters);
		return (SoloTender) response.getTender();
	}
	

	private SoloResponse executePostRequest(String endpoint, MultiValueMap<String, String> parameters) {
		
		String url = buildUri(rootUrl+endpoint,parameters);
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(null, new byte[]{});
		Request request = new Request.Builder().url(url).post(body).build();
		try {
			logger.debug("Calling url: " + url);
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				throw new RuntimeException("statusCode: " + response.code() + " body: " + response.body().string());
			}
			ObjectMapper mapper = new ObjectMapper();
			String responseBody = response.body().string();
			SoloResponse soloResponse = mapper.readValue(responseBody, SoloResponse.class);
			if (soloResponse.status != 0) {
				throw new RuntimeException("statusCode: " + response.code() + " body: " + responseBody);
			}
			return soloResponse;

			
		} catch (Exception e) {
			throw new RuntimeException( e.getMessage(), e);
		}
	}
	
	private SoloResponse executeGetRequest(String endpoint, MultiValueMap<String, String> parameters) {
		
		String url = buildUri(rootUrl+endpoint,parameters);
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(null, new byte[]{});
		Request request = new Request.Builder().url(url).post(body).build();
		try {
			logger.debug("Calling url: " + url);
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				throw new RuntimeException("statusCode: " + response.code() + " body: " + response.body().string());
			}
			ObjectMapper mapper = new ObjectMapper();
			String responseBody = response.body().string();
			SoloResponse soloResponse = mapper.readValue(responseBody, SoloResponse.class);
			if (soloResponse.status != 0) {
				throw new RuntimeException("statusCode: " + response.code() + " body: " + responseBody);
			}
			return soloResponse;

			
		} catch (Exception e) {
			throw new RuntimeException( e.getMessage(), e);
		}
	}


	private String buildUri(String url, MultiValueMap<String, String> params) {
	    UriComponents uriComponents = UriComponentsBuilder.newInstance()
	            .queryParams(params).build();

	   return url+uriComponents.toString();
	}
	
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class SoloResponse {
		@JsonProperty
		private int status;
		@JsonProperty
		private String message;
		
		@JsonProperty("racun")
		private Map<String, Object> invoice;
		
		@JsonProperty("ponuda")
		private Map<String, Object> tender;
		
		
		private SoloInvoice getInvoice() {
			SoloInvoice.Builder builder = new SoloInvoice.Builder();
			//invoice specific fields
			builder.number(invoice.get("broj_racuna").toString());
			if (invoice.get("zki") != null && invoice.get("jri") != null) {
				builder.isFiscal(true);
			} else {
				builder.isFiscal(false);
			}
						
			// common fields
			map(invoice, builder);
			return builder.build();
		}
		
		private SoloTender getTender() {
			SoloTender.Builder builder = new SoloTender.Builder();
			//invoice specific fields
			builder.number(tender.get("broj_ponude").toString());
						
			// common fields
			map(invoice, builder);
			return builder.build();
		}


		private void map(Map<String, Object> response, SoloBillingObject.Builder<?> builder) {
			builder.id(response.get("id").toString());
			builder.serviceType(response.get("tip_usluge").toString());
			builder.pdfUrl(response.get("pdf").toString());
			builder.email(response.get("kupac_naziv").toString());
			builder.note(response.get("napomene").toString());
			builder.paymentType(SoloPaymentType.getFromValue(
					Integer.parseInt(response.get("nacin_placanja").toString())));
			
			@SuppressWarnings("unchecked")
			List<Map<String, String>> taxes = (List<Map<String, String>>) response.get("porezi");
			builder.isTaxed(false);
			for(Map<String, String> taxing : taxes) {
				if (Integer.parseInt(taxing.get("stopa")) > 0) {
					builder.isTaxed(true);
				} 
			}
			
			
			@SuppressWarnings("unchecked")
			List<Map<String, String>> products = (List<Map<String, String>>) response.get("usluge");
			for (Map<String, String> product : products) {
				builder.addProduct(new SoloProduct.Builder()
						.name(product.get("opis_usluge"))
						.quantity(Integer.parseInt(product.get("kolicina")))
						.price(product.get("cijena"))
						.discount(product.get("popust"))
						.taxRate(product.get("porez_stopa"))
						.build());
			}
		}
	}

}
