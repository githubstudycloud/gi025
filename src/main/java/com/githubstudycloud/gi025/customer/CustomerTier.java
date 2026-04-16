package com.githubstudycloud.gi025.customer;

public enum CustomerTier {
	ENTERPRISE,
	GROWTH,
	STARTUP;

	public static CustomerTier fromNullable(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		return CustomerTier.valueOf(raw.trim().toUpperCase());
	}
}
