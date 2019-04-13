package org.bytepoet.shopifysolo.services;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;



public class CachedFunctionalService {

	
	private static Cache<MapKey, Object> cache = CacheBuilder.newBuilder()
		    .maximumSize(10000)
		    .expireAfterWrite(48, TimeUnit.HOURS)
		    .build();
	
	
	public static <OUT, IN> OUT cacheAndExecute(IN parameter, Function<IN,?> keyFunction, Function<IN,OUT> callback) {
		Object id = keyFunction.apply(parameter);
		MapKey mapKey = new MapKey(parameter.getClass(), id);
		if (putIfEmpty(mapKey, parameter)) {
			return callback.apply(parameter);
		}
		return null;
		
	}
	
	public static <IN> void cacheAndExecute(IN parameter, Function<IN,?> keyFunction, Consumer<IN> callback) {
		Object id = keyFunction.apply(parameter);
		MapKey mapKey = new MapKey(parameter.getClass(), id);
		if (putIfEmpty(mapKey, parameter)) {
			callback.accept(parameter);
		}
		
		return;
	}
	
	public static void clearCache() {
		cache.invalidateAll();
	}
	
	private static synchronized boolean putIfEmpty(MapKey mapKey, Object parameter) {
		if (cache.getIfPresent(mapKey) != null) {
			return false;
		}
		cache.put(mapKey, parameter);
		return true;
	}
	
	
	private static class MapKey {
	
		private Class<?> clazz;
		private Object id;
		
		
		private MapKey(Class<?> clazz, Object id) {
			super();
			this.clazz = clazz;
			this.id = id;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MapKey other = (MapKey) obj;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
		
		
	}

	
	
}
