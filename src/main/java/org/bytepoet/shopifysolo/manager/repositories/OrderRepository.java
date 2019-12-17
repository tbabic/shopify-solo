package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Optional;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
	
	@Query(value="select order from PaymentOrder order where order.shopifyOrderId = :shopifyId")
	Optional<PaymentOrder> getOrderWithShopifyId(@Param("shopifyId") String shopifyId);
}
