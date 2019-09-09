package org.bytepoet.shopifysolo.manager.repositories;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class OrderRepository extends AbstractSheetsRepository<Order, Long>{

	@Autowired
	public OrderRepository(@Value("${google.sheets.orders.id}") String sheetId) {
		super(sheetId);
	}

	@Override
	protected Class<Order> getType() {
		return Order.class;
	}

	@Override
	protected Long getId(Order data) {
		return data.getId();
	}

	
	
}
