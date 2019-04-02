package org.bytepoet.shopifysolo.solo.clients;

import java.util.Map;
import org.bytepoet.shopifysolo.solo.models.SoloReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import com.google.common.base.Joiner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



@Service
public class SoloApiClient {
	
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
		Request request = new Request.Builder().url(url).get().build();
		try {
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				response.body().string();
				throw new HttpServerErrorException(HttpStatus.valueOf(response.code()), response.body().string());
			}
			
		} catch (Exception e) {
			throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}


	}

}
