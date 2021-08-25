package org.bytepoet.shopifysolo;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import okhttp3.OkHttpClient;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder()
			    .connectTimeout(10, TimeUnit.SECONDS)
			    .writeTimeout(10, TimeUnit.SECONDS)
			    .readTimeout(60, TimeUnit.SECONDS)
			    .build();
	}
	
	

}
