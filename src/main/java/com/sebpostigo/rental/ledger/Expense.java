package com.sebpostigo.rental.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "expenses")
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long propertyId;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false)
	private ExpenseCategory category;

	private String notes;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Expense() {
	}

	public Expense(Long propertyId, LocalDate date, BigDecimal amount, ExpenseCategory category, String notes) {
		this.propertyId = propertyId;
		this.date = date;
		this.amount = amount;
		this.category = category;
		this.notes = notes;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public ExpenseCategory getCategory() {
		return category;
	}

	public String getNotes() {
		return notes;
	}

}
