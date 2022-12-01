package org.bytepoet.shopifysolo.manager.repositories;

import java.util.List;
import java.util.UUID;

import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.models.ProductPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPartRepository extends JpaRepository<ProductPart, UUID>, JpaSpecificationExecutor<Product>
{
	
	@Query(value="SELECT distinct part FROM ProductPart part "
			+ "LEFT JOIN FETCH part.distributions distro "
			+ "LEFT JOIN FETCH distro.product product "
			+ "LEFT JOIN FETCH product.partDistributions distro2 "
			+ "WHERE part.title LIKE %:search% "
			+ "OR part.description LIKE %:search% "
			+ "OR part.link LIKE %:search% "
			+ "OR part.alternativeDescription LIKE %:search% "
			+ "OR part.alternativeLink LIKE %:search% "
			+ "OR part.alternativeDescription2 LIKE %:search% "
			+ "OR part.alternativeLink2 LIKE %:search% ")
	List<ProductPart> searchProductParts(@Param("search") String search);
	
	@Query(value="SELECT distinct part FROM ProductPart part "
			+ "LEFT JOIN FETCH part.distributions distro "
			+ "LEFT JOIN FETCH distro.product product "
			+ "LEFT JOIN FETCH product.partDistributions distro2 ")
	List<ProductPart> findAllProductParts();

	
}
