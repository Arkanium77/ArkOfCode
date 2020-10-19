package team.isaz.ark.core.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import team.isaz.ark.core.entity.Book;

import java.util.List;

public interface BookRepository extends ElasticsearchRepository<Book, String> {
    Book findBookByTitle(String title);

    List<Book> findAll();

    @Query(value = "{\"bool\":{\"must\":{\"match\":{\"title\":\"?0\"}},\"should\":[{\"term\":{\"isHidden\":false }},{\"match\":{\"author\":\"?1\"}}]}}")
    List<Book> findAllByTitleAndAuthor(String title, String author);

    List<Book> findAllByTitleAndIsHiddenFalse(String title);
}
