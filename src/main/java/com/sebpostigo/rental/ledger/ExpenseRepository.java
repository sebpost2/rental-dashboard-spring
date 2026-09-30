package com.sebpostigo.rental.ledger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	List<Expense> findByPropertyIdAndDateBetweenOrderByDateAscIdAsc(Long propertyId, LocalDate start, LocalDate end);

	Optional<Expense> findByIdAndPropertyId(Long id, Long propertyId);

}
