package org.bytepoet.shopifysolo;

import org.bytepoet.shopifysolo.interceptors.ShopifyRequestInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebMvcConfig implements WebMvcConfigurer{

	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ShopifyRequestInterceptor());
    }
}
