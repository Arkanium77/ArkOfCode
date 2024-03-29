package team.isaz.ark.core.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import team.isaz.ark.core.aop.annotation.PrepareSnippet;
import team.isaz.ark.core.entity.Snippet;

import java.util.List;

public interface SnippetRepository extends ElasticsearchRepository<Snippet, String> {

    List<Snippet> findAllByAuthor(String author);

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @PrepareSnippet
    Snippet save(Snippet snippet);

}
