package com.githubstudycloud.gi025.ai;

import com.githubstudycloud.gi025.common.api.ApiResponse;
import com.githubstudycloud.gi025.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiChatController {

	private final AiAssistantService aiAssistantService;

	public AiChatController(AiAssistantService aiAssistantService) {
		this.aiAssistantService = aiAssistantService;
	}

	@PostMapping("/chat")
	public ApiResponse<AiApiModels.ChatResponse> chat(
			@Valid @RequestBody AiApiModels.ChatRequest request,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, aiAssistantService.chat(request));
	}
}
