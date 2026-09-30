package com.sebpostigo.rental.property;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

	List<Property> findByOwnerIdOrderByIdAsc(Long ownerId);

	Optional<Property> findByIdAndOwnerId(Long id, Long ownerId);

	/** One statement; the database cascades to incomes and expenses. */
	@Transactional
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from Property p where p.ownerId = :ownerId")
	void deleteByOwnerId(@Param("ownerId") Long ownerId);

}
