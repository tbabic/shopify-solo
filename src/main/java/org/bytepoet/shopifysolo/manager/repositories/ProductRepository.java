package org.bytepoet.shopifysolo.manager.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bytepoet.shopifysolo.manager.models.Product;
import org.bytepoet.shopifysolo.manager.models.ProductPart;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product>
{

	@Query(value="SELECT distinct product FROM Product product "
			+ "LEFT JOIN FETCH product.partDistributions distro "
			+ "LEFT JOIN FETCH distro.productPart part "
			+ "LEFT JOIN FETCH part.distributions distro2 "
			)
	public List<Product> findAndFetchAll();
	
	
	@Query(value="SELECT distinct product FROM Product product "
			+ "LEFT JOIN FETCH product.partDistributions distro "
			+ "LEFT JOIN FETCH distro.productPart part "
			+ "LEFT JOIN FETCH part.distributions distro2 "
			+ "WHERE lower(product.name) LIKE lower(:name)"
			)
	public List<Product> findAndFetchByNameLikeIgnoreCase(@Param("name") String name, Sort sort);
	
	@Query(value="SELECT distinct product FROM Product product "
			+ "LEFT JOIN FETCH product.partDistributions distro "
			+ "LEFT JOIN FETCH distro.productPart part "
			+ "WHERE product.webshopInfo.id = : id "
			)
	public Product findAndFetchByWebshopId(@Param("id")String id);
	
	@Query(value="SELECT distinct product FROM Product product "
			+ "LEFT JOIN FETCH product.partDistributions distro "
			+ "LEFT JOIN FETCH distro.productPart part "
			+ "WHERE product.webshopInfo.id IN (:ids) "
			)
	public List<Product> findAndFetchByWebshopIds(@Param("ids")List<String> ids);
	
	@Query(value="SELECT distinct product FROM Product product "
			+ "LEFT JOIN FETCH product.partDistributions distro "
			+ "LEFT JOIN FETCH distro.productPart part "
			+ "LEFT JOIN FETCH part.distributions distro2 "
			+ "WHERE product.id IN (:ids) "
			)
	public List<Product> findAndFetchByIds(@Param("ids")List<UUID> ids);
	
	@Query(value="SELECT distinct product FROM Product product "
			+ "LEFT JOIN FETCH product.partDistributions distro "
			+ "LEFT JOIN FETCH distro.productPart part "
			+ "LEFT JOIN FETCH part.distributions distro2 "
			+ "WHERE product.id = :id "
			)
	public Optional<Product> findAndFetchById(@Param("id")UUID id);
	
	@Query(value="SELECT distinct product FROM Product product "
			+ "WHERE product.webshopInfo.id = :id "
			)
	public Product findByWebshopId(@Param("id")String id);
	
	@Query(value="SELECT distinct product FROM Product product "
			+ "WHERE lower(product.name) LIKE lower(:name)"
			)
	public List<Product> findByNameLikeIgnoreCase(@Param("name")String name, Sort sort);
	
}
