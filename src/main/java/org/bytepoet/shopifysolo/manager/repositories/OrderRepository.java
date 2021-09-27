package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bytepoet.shopifysolo.manager.models.GiveawayOrder;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
	
	@Query(value="select order from PaymentOrder order where order.shopifyOrderId = :shopifyId")
	Optional<PaymentOrder> getPaymentOrderWithShopifyId(@Param("shopifyId") String shopifyId);
	
	@Query(value="select order from GiveawayOrder order where order.shopifyOrderId = :shopifyId")
	Optional<GiveawayOrder> getGiveawayOrderWithShopifyId(@Param("shopifyId") String shopifyId);
	
	@Query(value="select order from ManagedOrder order where order.shopifyOrderId = :shopifyId")
	Optional<Order> getOrderWithShopifyId(@Param("shopifyId") String shopifyId);
	
	@Query(value="select order from PaymentOrder order where order.id = :id")
	Optional<PaymentOrder> getPaymentOrderById(@Param("id") Long id);
	
	@Query(value="SELECT distinct o FROM ManagedOrder o "
			+ "INNER JOIN FETCH o.items i "
			+ "WHERE o.creationDate BETWEEN :start AND :end "
			+ "ORDER BY o.id ")
	List<Order> getByCreationDateBetween(@Param("start") Date start, @Param("end") Date end);
	
	@Query(value="SELECT distinct o FROM PaymentOrder o "
			+ "INNER JOIN FETCH o.items i "
			+ "WHERE o.invoice.date BETWEEN :start AND :end "
			+ "AND o.isPaid = true "
			+ "ORDER BY o.invoice.date ")
	List<PaymentOrder> getByPamentDateBetween(@Param("start") Date start, @Param("end") Date end);
	
	@Query(value="SELECT distinct o FROM ManagedOrder o "
			+ "LEFT JOIN FETCH o.items i "
			+ "WHERE o.status = :status ")
	List<Order> getByStatus(@Param("status") OrderStatus status);
}
