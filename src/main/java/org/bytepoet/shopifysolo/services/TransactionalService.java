package org.bytepoet.shopifysolo.services;

import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TransactionalService {

	public <OUT> Future<OUT> run(Supplier<OUT> callback) {
		OUT response = callback.get();
		return new AsyncResult<OUT>(response);
	}
	
	@Transactional
	public void run(Runnable callback) {
		callback.run();
	}
}
