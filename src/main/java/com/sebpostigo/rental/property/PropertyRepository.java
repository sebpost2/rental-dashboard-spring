package com.sebpostigo.rental.property;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {

	List<Property> findByOwnerIdOrderByIdAsc(Long ownerId);

	Optional<Property> findByIdAndOwnerId(Long id, Long ownerId);

}
