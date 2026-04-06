package team.isaz.ark.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.core.service.helper.ComplexRequestHelper;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    @Mock
    private ElasticsearchClientFacade elasticsearchClientFacade;

    @Mock
    private ComplexRequestHelper requestHelper;

    @Mock
    private AuthService authService;

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldSearchWithAuthenticatedUser() throws Exception {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        BoolQueryBuilder rawQuery = new BoolQueryBuilder();
        BoolQueryBuilder availableQuery = new BoolQueryBuilder();
        SearchResponse response = Mockito.mock(SearchResponse.class);
        SearchHit hit = new SearchHit(1, "s-1", null, null, null);
        hit.sourceRef(new BytesArray(mapper.writeValueAsString(Snippet.builder().title("Hello").text("World").tags(Set.of("java")).build())));
        SearchHits searchHits = new SearchHits(new SearchHit[]{hit}, null, 1.0f);
        Mockito.when(authService.checkToken("Bearer token")).thenReturn(new TokenCheck(Status.OK, "captain", "ROLE_USER"));
        Mockito.when(requestHelper.findByString("hello world")).thenReturn(rawQuery);
        Mockito.when(requestHelper.findAvailableSnippets("captain", rawQuery)).thenReturn(availableQuery);
        Mockito.when(elasticsearchClientFacade.search(Mockito.any())).thenReturn(response);
        Mockito.when(response.getHits()).thenReturn(searchHits);
        Mockito.when(response.getScrollId()).thenReturn("scroll-1");
        Mockito.when(elasticsearchClientFacade.clearScroll(Mockito.any(ClearScrollRequest.class)))
                .thenReturn(Mockito.mock(ClearScrollResponse.class));

        List<Snippet> snippets = searchService.search("Bearer token", "hello world");

        Assertions.assertThat(snippets).hasSize(1);
        Assertions.assertThat(snippets.getFirst().getId()).isEqualTo("s-1");
    }

    @Test
    void shouldReturnEmptyListWhenSearchClientFails() throws Exception {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        BoolQueryBuilder rawQuery = new BoolQueryBuilder();
        BoolQueryBuilder availableQuery = new BoolQueryBuilder();
        Mockito.when(requestHelper.findByString("hello")).thenReturn(rawQuery);
        Mockito.when(requestHelper.findAvailableSnippets(null, rawQuery)).thenReturn(availableQuery);
        Mockito.when(elasticsearchClientFacade.search(Mockito.any())).thenThrow(new IOException("boom"));

        Assertions.assertThat(searchService.search("hello")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenDocumentCannotBeParsed() throws Exception {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, new BrokenMapper(), requestHelper, authService);
        BoolQueryBuilder rawQuery = new BoolQueryBuilder();
        BoolQueryBuilder availableQuery = new BoolQueryBuilder();
        SearchResponse response = Mockito.mock(SearchResponse.class);
        SearchHit hit = new SearchHit(1, "s-1", null, null, null);
        hit.sourceRef(new BytesArray("{\"bad\":true}"));
        SearchHits searchHits = new SearchHits(new SearchHit[]{hit}, null, 1.0f);
        Mockito.when(requestHelper.findByString("hello")).thenReturn(rawQuery);
        Mockito.when(requestHelper.findAvailableSnippets(null, rawQuery)).thenReturn(availableQuery);
        Mockito.when(elasticsearchClientFacade.search(Mockito.any())).thenReturn(response);
        Mockito.when(response.getHits()).thenReturn(searchHits);
        Mockito.when(response.getScrollId()).thenReturn("scroll-1");
        Mockito.when(elasticsearchClientFacade.clearScroll(Mockito.any(ClearScrollRequest.class)))
                .thenReturn(Mockito.mock(ClearScrollResponse.class));

        Assertions.assertThat(searchService.search("hello")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenClearScrollFails() throws Exception {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        BoolQueryBuilder rawQuery = new BoolQueryBuilder();
        BoolQueryBuilder availableQuery = new BoolQueryBuilder();
        SearchResponse response = Mockito.mock(SearchResponse.class);
        SearchHit hit = new SearchHit(1, "s-1", null, null, null);
        hit.sourceRef(new BytesArray(mapper.writeValueAsString(Snippet.builder().title("Hello").text("World").build())));
        SearchHits searchHits = new SearchHits(new SearchHit[]{hit}, null, 1.0f);
        Mockito.when(requestHelper.findByString("hello")).thenReturn(rawQuery);
        Mockito.when(requestHelper.findAvailableSnippets(null, rawQuery)).thenReturn(availableQuery);
        Mockito.when(elasticsearchClientFacade.search(Mockito.any())).thenReturn(response);
        Mockito.when(response.getHits()).thenReturn(searchHits);
        Mockito.when(response.getScrollId()).thenReturn("scroll-1");
        Mockito.when(elasticsearchClientFacade.clearScroll(Mockito.any(ClearScrollRequest.class)))
                .thenThrow(new IOException("boom"));

        Assertions.assertThat(searchService.search("hello")).hasSize(1);
    }

    @Test
    void shouldRejectSearchForInvalidToken() {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        Mockito.when(authService.checkToken("Bearer token")).thenReturn(new TokenCheck(Status.ERROR, null, null));

        Assertions.assertThatThrownBy(() -> searchService.search("Bearer token", "hello"))
                .isInstanceOf(AuthenticationSin.class);
    }

    @Test
    void shouldReturnSnippetForOwner() {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        Snippet snippet = Snippet.builder().id("s-1").author("captain").hidden(true).build();
        Mockito.when(authService.getLogin("Bearer token")).thenReturn("captain");
        Mockito.when(snippetRepository.findById("s-1")).thenReturn(Optional.of(snippet));

        Assertions.assertThat(searchService.get("Bearer token", "s-1")).isSameAs(snippet);
    }

    @Test
    void shouldRejectHiddenSnippetForAnotherUser() {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        Snippet snippet = Snippet.builder().id("s-1").author("other").hidden(true).build();
        Mockito.when(authService.getLogin("Bearer token")).thenReturn("captain");
        Mockito.when(snippetRepository.findById("s-1")).thenReturn(Optional.of(snippet));

        Assertions.assertThatThrownBy(() -> searchService.get("Bearer token", "s-1"))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("hidden");
    }

    @Test
    void shouldRejectMissingSnippetForAuthorizedRequest() {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        Mockito.when(authService.getLogin("Bearer token")).thenReturn("captain");
        Mockito.when(snippetRepository.findById("missing")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> searchService.get("Bearer token", "missing"))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("was not found");
    }

    @Test
    void shouldReturnPublicSnippet() {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        Snippet snippet = Snippet.builder().id("s-1").hidden(false).build();
        Mockito.when(snippetRepository.findById("s-1")).thenReturn(Optional.of(snippet));

        Assertions.assertThat(searchService.get("s-1")).isSameAs(snippet);
    }

    @Test
    void shouldRejectHiddenPublicSnippet() {
        SearchService searchService = new SearchService(snippetRepository, elasticsearchClientFacade, mapper, requestHelper, authService);
        Snippet snippet = Snippet.builder().id("s-1").hidden(true).build();
        Mockito.when(snippetRepository.findById("s-1")).thenReturn(Optional.of(snippet));

        Assertions.assertThatThrownBy(() -> searchService.get("s-1"))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("is hidden");
    }

    private static final class BrokenMapper extends ObjectMapper {
        @Override
        public <T> T readValue(String content, Class<T> valueType) throws JsonProcessingException {
            throw new JsonProcessingException("broken", new NullPointerException("cause")) {
            };
        }
    }
}

