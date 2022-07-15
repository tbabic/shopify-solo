package org.bytepoet.shopifysolo.manager.repositories;

import java.util.List;
import java.util.UUID;

import org.bytepoet.shopifysolo.manager.models.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product>
{

	public List<Product> findByNameLikeIgnoreCase(String name, Sort sort);

	
}
