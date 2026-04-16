package com.githubstudycloud.gi025.knowledge;

import com.githubstudycloud.gi025.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "knowledge_articles")
public class KnowledgeArticle extends BaseEntity {

	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false, length = 64)
	private String topic;

	@Lob
	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private boolean published = true;
}
