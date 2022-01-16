package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Date;
import java.util.List;

import org.bytepoet.shopifysolo.manager.models.SocialMediaPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMediaPostRepository extends JpaRepository<SocialMediaPost, Long>{

	
	@Query(value="SELECT distinct post FROM SocialMediaPost post "
			+ "WHERE post.date BETWEEN :start AND :end "
			+ "ORDER BY post.date, post.orderPosition ")
	List<SocialMediaPost> getByDateBetween(@Param("start") Date start, @Param("end") Date end);
}
