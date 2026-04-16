package com.githubstudycloud.gi025.knowledge;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeService {

	private final KnowledgeArticleRepository articleRepository;

	public KnowledgeService(KnowledgeArticleRepository articleRepository) {
		this.articleRepository = articleRepository;
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "knowledge-search", key = "'PUBLISHED'")
	public List<KnowledgeApiModels.ArticleView> listPublishedArticles() {
		return articleRepository.findAllByPublishedTrueOrderByTopicAscTitleAsc().stream().map(this::toView).toList();
	}

	@Transactional(readOnly = true)
	public List<KnowledgeApiModels.ArticleView> search(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return listPublishedArticles();
		}
		return articleRepository.searchPublished(keyword.trim()).stream().map(this::toView).toList();
	}

	private KnowledgeApiModels.ArticleView toView(KnowledgeArticle article) {
		return new KnowledgeApiModels.ArticleView(article.getSlug(), article.getTitle(), article.getTopic(), article.getContent());
	}
}
