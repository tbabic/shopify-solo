package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Date;
import java.util.List;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderArchiveRepository extends JpaRepository<OrderArchive, Long>, JpaSpecificationExecutor<Order> {
	
	public List<OrderArchive> getByStartingAfterAndEndingBefore(Date start, Date end);
	
	
}
