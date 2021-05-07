package team.isaz.ark.backup.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import team.isaz.ark.backup.entity.Snippet;

public interface SnippetRepository extends ElasticsearchRepository<Snippet, String> {
}
