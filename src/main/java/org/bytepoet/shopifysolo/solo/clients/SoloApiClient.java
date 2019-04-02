package org.bytepoet.shopifysolo.solo.clients;

import java.util.Map;
import org.bytepoet.shopifysolo.solo.models.SoloReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

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
	
	
	public void createReceipt(SoloReceipt receipt) {
		String endpoint = "/racun";
		
		Map<String, String> parameters = mapper.map(receipt);
		parameters.put("token", apiToken);
		
		String queryParams = Joiner.on("&").withKeyValueSeparator("=")
	            .join(parameters);
		
		String url = new StringBuilder(rootUrl)
				.append(endpoint)
				.append("?")
				.append(queryParams)
				.toString();
				
		
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(null, new byte[]{});
		Request request = new Request.Builder().url(url).post(body).build();
		try {
			logger.debug("Calling url: " + url);
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				throw new HttpServerErrorException(HttpStatus.valueOf(response.code()), response.body().string());
			}
			ObjectMapper mapper = new ObjectMapper();
			SoloResponse soloResponse = mapper.readValue(response.body().string(), SoloResponse.class);
			if (soloResponse.status != 0) {
				throw new HttpServerErrorException(HttpStatus.valueOf(response.code()), response.body().string());
			}
			
		} catch (Exception e) {
			throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}


	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class SoloResponse {
		@JsonProperty
		private int status;
		@JsonProperty
		private String message;
		
		@JsonProperty
		private Map<String, Object> racun;
	}

}
