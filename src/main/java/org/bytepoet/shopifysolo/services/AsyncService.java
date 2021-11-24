package org.bytepoet.shopifysolo.services;

import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

	@Async
	public <OUT> Future<OUT> future(Supplier<OUT> callback) {
		OUT response = callback.get();
		return new AsyncResult<OUT>(response);
	}
	
	@Async
	public void run(Runnable callback) {
		callback.run();
	}
}
