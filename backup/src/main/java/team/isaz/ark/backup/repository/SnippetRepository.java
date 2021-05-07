package team.isaz.ark.backup.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import team.isaz.ark.backup.aop.annotation.PrepareSnippet;
import team.isaz.ark.backup.entity.Snippet;

public interface SnippetRepository extends ElasticsearchRepository<Snippet, String> {
    @PrepareSnippet
    Snippet save(Snippet snippet);


}
