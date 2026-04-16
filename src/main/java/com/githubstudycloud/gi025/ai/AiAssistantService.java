package com.githubstudycloud.gi025.ai;

import com.githubstudycloud.gi025.common.exception.BusinessException;
import com.githubstudycloud.gi025.config.EnterpriseProperties;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantService {

	private final EnterpriseProperties properties;

	private final ObjectProvider<ChatModel> chatModelProvider;

	private final ObjectProvider<ToolCallbackProvider> toolCallbackProviders;

	public AiAssistantService(
			EnterpriseProperties properties,
			ObjectProvider<ChatModel> chatModelProvider,
			ObjectProvider<ToolCallbackProvider> toolCallbackProviders) {
		this.properties = properties;
		this.chatModelProvider = chatModelProvider;
		this.toolCallbackProviders = toolCallbackProviders;
	}

	public AiApiModels.ChatResponse chat(AiApiModels.ChatRequest request) {
		ChatModel chatModel = chatModelProvider.getIfAvailable();
		if (chatModel == null) {
			throw new BusinessException("AI chat is disabled. Activate the openai profile and provide OPENAI_API_KEY.");
		}

		ChatClient.ChatClientRequestSpec prompt = ChatClient.create(chatModel)
			.prompt()
			.system(properties.getAi().getSystemPrompt())
			.user(request.message());

		List<ToolCallbackProvider> providers = toolCallbackProviders.orderedStream().toList();
		boolean attachRemoteTools = request.enableRemoteTools() && !providers.isEmpty();
		if (attachRemoteTools) {
			prompt = prompt.toolCallbacks(providers.toArray(ToolCallbackProvider[]::new));
		}

		String reply = prompt.call().content();
		return new AiApiModels.ChatResponse(reply, attachRemoteTools, providers.size());
	}
}
