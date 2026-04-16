package com.githubstudycloud.gi025.knowledge;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, UUID> {

	List<KnowledgeArticle> findAllByPublishedTrueOrderByTopicAscTitleAsc();

	@Query("""
		select article
		from KnowledgeArticle article
		where article.published = true
		  and (
		    lower(article.title) like lower(concat('%', :keyword, '%'))
		    or lower(article.topic) like lower(concat('%', :keyword, '%'))
		  )
		order by article.topic asc, article.title asc
		""")
	List<KnowledgeArticle> searchPublished(@Param("keyword") String keyword);
}
