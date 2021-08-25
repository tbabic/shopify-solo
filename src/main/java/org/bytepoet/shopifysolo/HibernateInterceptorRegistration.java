package org.bytepoet.shopifysolo;

import java.util.Map;

import org.bytepoet.shopifysolo.interceptors.HibernateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
public class HibernateInterceptorRegistration implements HibernatePropertiesCustomizer {
	
	@Lazy
	@Autowired
    private HibernateInterceptor hibernateInterceptor;

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.session_factory.interceptor", hibernateInterceptor);
    }
}
