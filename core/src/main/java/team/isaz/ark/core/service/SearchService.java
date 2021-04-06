package team.isaz.ark.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.ClearScrollRequest;
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
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.core.service.helper.ComplexRequestHelper;
import team.isaz.ark.libs.sinsystem.model.ArkOfSinCodes;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final SnippetRepository snippetRepository;
    private final @Qualifier("elasticHighLevelClient")
    RestHighLevelClient client;
    private final ObjectMapper mapper;
    private final ComplexRequestHelper requestHelper;
    private final AuthService authService;

    public List<Snippet> all() {
        return Lists.newArrayList(snippetRepository.findAll());
    }

    public List<Snippet> allAvailable(String author) {
        return snippetRepository.findAllByAuthorOrHiddenFalse(author);
    }

    public List<Snippet> find(String author, String query) {
        List<SearchHit> hits = trySearch(
                requestHelper.findAvailableSnippets(author, requestHelper.findByString(query)));
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
                                                     ).scroll(TimeValue.timeValueMinutes(1L)),
                                             RequestOptions.DEFAULT);

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(r.getScrollId());
            try {
                client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("Can't clear scroll context with id=<{}>", r.getScrollId());
            }
            return Arrays.asList(r.getHits().getHits());
        } catch (Exception e) {
            log.error("Caught {} when trying search by query {}.\n Message: {}", e.getClass().getSimpleName(),
                      query.toString(), e
                              .getMessage());
            return Collections.emptyList();
        }
    }

    private Function<SearchHit, Snippet> hitToSnippet() {
        return hit -> {
            try {
                return mapper.readValue(hit.getSourceAsString(), Snippet.class)
                        .withId(hit.getId());
            } catch (JsonProcessingException e) {
                log.error("{}: {}. \n Cause: {}", e.getClass().getSimpleName(), e.getMessage(),
                          e.getCause().getMessage());
                return null;
            }
        };
    }

    public List<Snippet> search(String bearerToken, String query) {
        TokenCheck t = authService.checkToken(bearerToken);
        if (Status.ERROR.equals(t.getStatus())) {
            throw new AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11001);
        }
        String login = t.getLogin();
        return find(login, query);
    }

    public List<Snippet> search(String query) {
        return find(null, query);
    }
}
