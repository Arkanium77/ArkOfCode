package team.isaz.ark.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.core.service.helper.ComplexRequestHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {
    private final SnippetRepository snippetRepository;
    private final RestHighLevelClient client;
    private final ObjectMapper mapper;
    private final ComplexRequestHelper requestHelper;

    public SearchService(SnippetRepository snippetRepository, @Qualifier("elasticHighLevelClient") RestHighLevelClient client, ObjectMapper mapper, ComplexRequestHelper requestHelper) {
        this.snippetRepository = snippetRepository;
        this.client = client;
        this.mapper = mapper;
        this.requestHelper = requestHelper;
    }

    public List<Snippet> all() {
        return Lists.newArrayList(snippetRepository.findAll());
    }

    public List<Snippet> allAvailable(String author) {
        return snippetRepository.findAllByAuthorOrHiddenFalse(author);
    }

    public List<Snippet> find(String author, String search) {
        List<SearchHit> hits = trySearch(requestHelper.findAvailableSnippets(author, requestHelper.findByString(search)));
        return hits.stream()
                .map(hitToSnippet())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<SearchHit> trySearch(BoolQueryBuilder query) {
        try {
            SearchResponse r = client.search(new SearchRequest("snippets")
                    .source(new SearchSourceBuilder()
                            .query(query)
                    ).scroll(TimeValue.timeValueMinutes(1L)), RequestOptions.DEFAULT);
            return Arrays.asList(r.getHits().getHits());
        } catch (Exception e) {
            log.error("Caught {} when trying search by query {}.\n Message: {}", e.getClass().getSimpleName(), query.toString(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private Function<SearchHit, Snippet> hitToSnippet() {
        return hit -> {
            try {
                return mapper.readValue(hit.getSourceAsString(), Snippet.class)
                        .withId(hit.getId());
            } catch (JsonProcessingException e) {
                log.error("{}: {}. \n Cause: {}", e.getClass().getSimpleName(), e.getMessage(), e.getCause().getMessage());
                return null;
            }
        };
    }
}
