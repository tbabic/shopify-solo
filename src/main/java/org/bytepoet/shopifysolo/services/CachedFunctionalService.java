package org.bytepoet.shopifysolo.services;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;



public class CachedFunctionalService {
	
	private static final Logger logger = LoggerFactory.getLogger(CachedFunctionalService.class);
	
	private static Cache<MapKey, Object> cache = CacheBuilder.newBuilder()
		    .maximumSize(10000)
		    .expireAfterWrite(48, TimeUnit.HOURS)
		    .build();
	
	private static Cache<MapKey, Executor> executors = CacheBuilder.newBuilder()
		    .maximumSize(10000)
		    .expireAfterWrite(48, TimeUnit.HOURS)
		    .build();
	
	public static <IN, OUT> OUT cacheAndExecute(IN parameter, Function<IN,?> keyFunction, Function<IN, OUT> callback) {
		Object id = keyFunction.apply(parameter);
		MapKey mapKey = new MapKey(parameter.getClass(), id);
		Executor executor = getExecutor(mapKey);
		OUT result = executor.execute(mapKey, parameter, callback);
		return result;
	}
	
	public static <IN> void cacheAndExecute(IN parameter, Function<IN,?> keyFunction, Consumer<IN> callback) {
		Object id = keyFunction.apply(parameter);
		MapKey mapKey = new MapKey(parameter.getClass(), id);
		Executor executor = getExecutor(mapKey);
		executor.execute(mapKey, parameter, callback);
		return;
	}
	
	public static void clearCache() {
		cache.invalidateAll();
	}
	
	private synchronized static Executor getExecutor(MapKey mapKey) {
		logger.info("fetching executor");
		Executor executor = executors.getIfPresent(mapKey);
		if (executor == null) {
			logger.info("new executor");
			executor = new Executor();
			executors.put(mapKey, executor);
		}
		return executor;
	}
	
	private static class Executor {
		
		@SuppressWarnings("unchecked")
		private synchronized <IN, OUT> OUT execute(MapKey mapKey, IN parameter, Function<IN, OUT> callback) {
			try {
				logger.debug("starting execution");
				Object cached = cache.getIfPresent(mapKey);
				if (cached != null) {
					logger.debug("skipping execution");
					// already executed
					return (OUT) cached;
				}
				logger.debug("processing execution");
				OUT result = callback.apply(parameter);
				cache.put(mapKey, result);
				logger.debug("execution finished");
				return result;
			} catch (Exception e) {
				throw e;
			}
			
		}
		
		
		private synchronized <IN, OUT> void execute(MapKey mapKey, IN parameter, Consumer<IN> callback) {
			try {
				logger.debug("starting execution");
				if (cache.getIfPresent(mapKey) != null) {
					logger.info("skipping execution");
					// already executed
					return;
				}
				logger.debug("processing execution");
				callback.accept(parameter);
				cache.put(mapKey, parameter);
				logger.debug("execution finished");
				return;
			} catch (Exception e) {
				throw e;
			}
			
		}
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
