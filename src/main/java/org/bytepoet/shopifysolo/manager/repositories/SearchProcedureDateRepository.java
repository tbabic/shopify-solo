package org.bytepoet.shopifysolo.manager.repositories;

import java.util.UUID;

import org.bytepoet.shopifysolo.manager.models.SearchProcedureDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchProcedureDateRepository extends JpaRepository<SearchProcedureDate, UUID>{
	
	public SearchProcedureDate findTopByOrderByDateEndDesc();
	
}
