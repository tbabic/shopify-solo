package org.bytepoet.shopifysolo.manager.utils;

import java.util.List;

import javax.transaction.Transactional;

import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.manager.repositories.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefundTransactionalService {

	@Autowired
	private RefundRepository refundRepository;
	
	@Transactional
	public List<Refund> findAll() {
		 List<Refund> refunds = refundRepository.findAll();
		 refunds.stream().forEach(refund -> refund.getItems().size());
		 return refunds;
	}
	
	@Transactional
	public void deleteAll() {
		List<Refund> refunds = refundRepository.findAll();
		refunds.stream().forEach(refund -> {
			refund.getItems().stream().forEach(item -> item.setRefund(null));
			refund.getItems().clear();
			
			refundRepository.saveAndFlush(refund);
		});
		refundRepository.deleteAll();
		
	}
}
