package com.githubstudycloud.gi025.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class CustomerApiModels {

	private CustomerApiModels() {
	}

	public record CreateCustomerRequest(
			@NotBlank(message = "name is required") String name,
			@Email(message = "email must be valid") @NotBlank(message = "email is required") String email,
			@NotBlank(message = "country is required") String country,
			CustomerTier tier) {
	}

	public record CustomerView(
			String customerNo,
			String name,
			String email,
			String country,
			CustomerTier tier,
			CustomerStatus status,
			Instant createdAt) {
	}
}
