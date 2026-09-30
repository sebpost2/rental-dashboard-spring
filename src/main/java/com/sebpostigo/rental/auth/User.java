package com.sebpostigo.rental.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String hashedPassword;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected User() {
	}

	public User(String email, String hashedPassword) {
		this.email = email;
		this.hashedPassword = hashedPassword;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void changePassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

}
