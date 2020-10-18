package org.bytepoet.shopifysolo.manager.repositories;

import java.util.List;

import org.bytepoet.shopifysolo.manager.models.Inventory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory>{

	public List<Inventory> findByItemLikeIgnoreCase(String item, Sort sort);
}
