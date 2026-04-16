package com.githubstudycloud.gi025.common.api;

import java.time.Instant;

public record ApiResponse<T>(boolean success, Instant timestamp, String requestId, T data) {

	public static <T> ApiResponse<T> success(String requestId, T data) {
		return new ApiResponse<>(true, Instant.now(), requestId, data);
	}
}
