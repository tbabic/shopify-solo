package org.bytepoet.shopifysolo.manager.repositories;

import java.util.List;

import org.bytepoet.shopifysolo.manager.models.TextRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TextRecordRepository extends JpaRepository<TextRecord, Long>{

	@Query("SELECT DISTINCT tr.category from TextRecord tr order by tr.category asc")
	List<String> getAllCategories();
	
	List<TextRecord> findByCategory(String category);
}
