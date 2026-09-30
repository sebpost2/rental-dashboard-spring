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
@Table(name = "incomes")
public class Income {

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
	private String source;

	private String notes;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Income() {
	}

	public Income(Long propertyId, LocalDate date, BigDecimal amount, String source, String notes) {
		this.propertyId = propertyId;
		this.date = date;
		this.amount = amount;
		this.source = source;
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

	public String getSource() {
		return source;
	}

	public String getNotes() {
		return notes;
	}

}
