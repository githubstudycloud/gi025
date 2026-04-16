package com.githubstudycloud.gi025.knowledge;

public final class KnowledgeApiModels {

	private KnowledgeApiModels() {
	}

	public record ArticleView(String slug, String title, String topic, String content) {
	}
}
