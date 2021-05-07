package team.isaz.ark.backup.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import team.isaz.ark.backup.aop.annotation.PrepareSnippet;
import team.isaz.ark.backup.entity.Snippet;

import java.util.List;

public interface SnippetRepository extends ElasticsearchRepository<Snippet, String> {
    List<Snippet> findAllByAuthorOrHiddenFalse(String author);

    List<Snippet> findAllByAuthor(String author);

    @PrepareSnippet
    Snippet save(Snippet snippet);

}
