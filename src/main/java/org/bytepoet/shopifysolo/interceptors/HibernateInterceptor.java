package org.bytepoet.shopifysolo.interceptors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bytepoet.shopifysolo.manager.models.AuditLog;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.repositories.AuditLogRepository;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;

@Lazy
@Component
public class HibernateInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = -4576969554356208420L;

	@Autowired
	private AuditLogRepository auditLogRepository;


	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {

		if (entity instanceof Order) {

			
			Map<String, Object> currentMap = new LinkedHashMap<>();
			Map<String, Object> previousMap = new LinkedHashMap<>();
			
			currentMap.put("id", ((Order)entity).getId());
			previousMap.put("id", ((Order)entity).getId());
			
			for (int i = 0; i<propertyNames.length; i++) {
				currentMap.put(propertyNames[i], currentState[i]);
				previousMap.put(propertyNames[i], previousState[i]);
			}
	
			
			if(!Objects.equal(currentMap.get("status"), previousMap.get("status"))) {
				Map<String, Object> currentMinimalMap = new LinkedHashMap<>();
				Map<String, Object> previousMinimalMap = new LinkedHashMap<>();
				List<String> keys = Arrays.asList("id", "contact", "shopifyOrderNumber", "status");
				
				copyMaps(currentMap, currentMinimalMap, keys);
				copyMaps(previousMap, previousMinimalMap, keys);
				
				AuditLog auditLog = new AuditLog();
				auditLog.setNext(currentMinimalMap);
				auditLog.setPrevious(previousMinimalMap);
				auditLog.setLogTime(new Date());
				
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				auditLog.setChangedBy(authentication.getName());
				auditLogRepository.save(auditLog);
			}
			
			
		}
		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);

	}
	
	private void copyMaps(Map<String, Object> source, Map<String, Object> target, List<String> keys) {
		for (String key : keys) {
			target.put(key, source.get(key));
		}
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] currentState, String[] propertyNames, Type[] types) {

		if (entity instanceof Order) {

		}

		return super.onSave(entity, id, currentState, propertyNames, types);

	}

}
