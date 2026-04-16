package com.githubstudycloud.gi025.common.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
		boolean success,
		Instant timestamp,
		String requestId,
		String code,
		String message,
		String path,
		Map<String, String> details) {

	public static ApiErrorResponse of(
			String requestId,
			String code,
			String message,
			String path,
			Map<String, String> details) {
		return new ApiErrorResponse(false, Instant.now(), requestId, code, message, path, details);
	}
}
