package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Date;
import java.util.List;

import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long>{

	List<Refund> getByInvoiceDateBetween(Date start, Date end);
}
