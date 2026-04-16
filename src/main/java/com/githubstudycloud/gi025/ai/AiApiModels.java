package com.githubstudycloud.gi025.ai;

import jakarta.validation.constraints.NotBlank;

public final class AiApiModels {

	private AiApiModels() {
	}

	public record ChatRequest(
			@NotBlank(message = "message is required") String message,
			boolean enableRemoteTools) {
	}

	public record ChatResponse(String reply, boolean remoteToolsAttached, int toolProviderCount) {
	}
}
