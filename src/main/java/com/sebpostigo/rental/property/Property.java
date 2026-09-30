package com.sebpostigo.rental.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "properties")
public class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long ownerId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String propertyType;

	@Column(nullable = false)
	private String currency;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Property() {
	}

	public Property(Long ownerId, String name, String address, String propertyType, String currency) {
		this.ownerId = ownerId;
		this.name = name;
		this.address = address;
		this.propertyType = propertyType;
		this.currency = currency;
	}

	/** PATCH semantics: null means "not provided", so the field keeps its value. */
	public void update(String name, String address, String propertyType, String currency) {
		if (name != null) {
			this.name = name;
		}
		if (address != null) {
			this.address = address;
		}
		if (propertyType != null) {
			this.propertyType = propertyType;
		}
		if (currency != null) {
			this.currency = currency;
		}
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public String getCurrency() {
		return currency;
	}

}
