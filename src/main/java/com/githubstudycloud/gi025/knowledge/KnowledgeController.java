package com.githubstudycloud.gi025.knowledge;

import com.githubstudycloud.gi025.common.api.ApiResponse;
import com.githubstudycloud.gi025.common.web.RequestIdFilter;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

	private final KnowledgeService knowledgeService;

	public KnowledgeController(KnowledgeService knowledgeService) {
		this.knowledgeService = knowledgeService;
	}

	@GetMapping
	public ApiResponse<List<KnowledgeApiModels.ArticleView>> listKnowledge(
			@RequestParam(required = false, name = "q") String keyword,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, knowledgeService.search(keyword));
	}
}
