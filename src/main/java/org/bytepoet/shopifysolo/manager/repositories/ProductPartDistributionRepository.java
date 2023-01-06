package org.bytepoet.shopifysolo.manager.repositories;

import java.util.UUID;

import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.models.ProductPartDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductPartDistributionRepository extends JpaRepository<ProductPartDistribution, UUID>, JpaSpecificationExecutor<ProductPartDistribution>
{
	
}
