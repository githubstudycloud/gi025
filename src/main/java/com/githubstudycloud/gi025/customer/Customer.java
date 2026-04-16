package com.githubstudycloud.gi025.customer;

import com.githubstudycloud.gi025.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

	@Column(name = "customer_no", nullable = false, unique = true, length = 32)
	private String customerNo;

	@Column(nullable = false, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CustomerTier tier;

	@Column(nullable = false, unique = true, length = 160)
	private String email;

	@Column(nullable = false, length = 64)
	private String country;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CustomerStatus status;
}
