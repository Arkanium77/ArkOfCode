package team.isaz.ark.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.assertj.core.api.Assertions;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import team.isaz.ark.core.CoreApplication;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Testcontainers
@SpringBootTest(classes = CoreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CoreControllerIntegrationTest {

    @Container
    private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER =
            new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.3"))
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("discovery.type", "single-node");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.discovery.enabled", () -> true);
        registry.add("feign.user.name", () -> "ark-user-test");
        registry.add("spring.cloud.discovery.client.simple.instances.ark-user-test[0].uri", wireMock::baseUrl);
        registry.add("elastic-search.hosts[0]", ELASTICSEARCH_CONTAINER::getHost);
        registry.add("elastic-search.ports[0]", () -> ELASTICSEARCH_CONTAINER.getMappedPort(9200));
        registry.add("elastic-search.scheme", () -> "http");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @AfterEach
    void cleanUp() throws Exception {
        snippetRepository.deleteAll();
        refreshIndex();
        wireMock.resetAll();
    }

    @Test
    void shouldServePublicAndSecuredControllerFlows() throws Exception {
        stubUserToken("Bearer user-token", "captain", "ROLE_USER");
        stubUserToken("Bearer service-token", "_tech", "ROLE_SERVICE");
        Snippet publicSnippet = snippetRepository.save(Snippet.builder().author("captain").hidden(false).title("Java title").text("Java body").tags(Set.of("java")).build());
        Snippet hiddenSnippet = snippetRepository.save(Snippet.builder().author("captain").hidden(true).title("Hidden").text("Secret").tags(Set.of("secret")).build());
        refreshIndex();

        MvcResult publicSearchResult = mockMvc.perform(MockMvcRequestBuilders.get("/public/search").param("query", "Java"))
                .andReturn();
        Assertions.assertThat(publicSearchResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(publicSearchResult.getResponse().getContentAsString()).contains("Java title");

        MvcResult publicGetResult = mockMvc.perform(MockMvcRequestBuilders.get("/public/get/{snippetId}", publicSnippet.getId()))
                .andReturn();
        Assertions.assertThat(publicGetResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(publicGetResult.getResponse().getContentAsString()).contains(publicSnippet.getId());

        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.get("/secured")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andReturn();
        Assertions.assertThat(loginResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(loginResult.getResponse().getContentAsString()).contains("captain");

        MvcResult securedSearchResult = mockMvc.perform(MockMvcRequestBuilders.get("/secured/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .param("query", "Hidden"))
                .andReturn();
        Assertions.assertThat(securedSearchResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(securedSearchResult.getResponse().getContentAsString()).contains(hiddenSnippet.getId());

        MvcResult securedGetResult = mockMvc.perform(MockMvcRequestBuilders.get("/secured/get/{snippetId}", hiddenSnippet.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andReturn();
        Assertions.assertThat(securedGetResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(securedGetResult.getResponse().getContentAsString()).contains(hiddenSnippet.getId());

        MvcResult publishResult = mockMvc.perform(MockMvcRequestBuilders.post("/secured/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .param("Статус сокрытия", "true")
                        .param("Название сниппета", "Published")
                        .param("Теги", "spring", "boot")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Body"))
                .andReturn();
        Assertions.assertThat(publishResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(publishResult.getResponse().getContentAsString()).contains("Snippet successfully saved");

        Snippet createdSnippet = snippetRepository.findAllByAuthor("captain").stream()
                .filter(snippet -> "Published".equals(snippet.getTitle()))
                .findFirst()
                .orElseThrow();

        MvcResult updateBlankTextResult = mockMvc.perform(MockMvcRequestBuilders.put("/secured/update/{snippetId}", createdSnippet.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .param("Название сниппета", "Updated title")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("   "))
                .andReturn();
        Assertions.assertThat(updateBlankTextResult.getResponse().getStatus()).isEqualTo(200);

        MvcResult updateTextResult = mockMvc.perform(MockMvcRequestBuilders.put("/secured/update/{snippetId}", createdSnippet.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .param("Теги", "java")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Updated body"))
                .andReturn();
        Assertions.assertThat(updateTextResult.getResponse().getStatus()).isEqualTo(200);

        Snippet updatableSnippet = snippetRepository.save(Snippet.builder().author("old_login").hidden(false).title("Legacy").text("Body").build());
        refreshIndex();
        MvcResult internalResult = mockMvc.perform(MockMvcRequestBuilders.put("/internal/snippets/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer service-token")
                        .param("login", "old_login")
                        .param("newLogin", "new_login"))
                .andReturn();
        Assertions.assertThat(internalResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(snippetRepository.findById(updatableSnippet.getId()).orElseThrow().getAuthor()).isEqualTo("new_login");

        MvcResult errorResult = mockMvc.perform(MockMvcRequestBuilders.get("/public/error")).andReturn();
        Assertions.assertThat(errorResult.getResponse().getStatus()).isEqualTo(403);
    }

    private void stubUserToken(String bearerToken, String login, String role) {
        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/internal/bearer/check"))
                .withQueryParam("bearerToken", WireMock.equalTo(bearerToken))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"status\":\"OK\",\"login\":\"" + login + "\",\"role\":\"" + role + "\"}")));
    }

    private void refreshIndex() throws Exception {
        restHighLevelClient.indices().refresh(new RefreshRequest("snippets"), RequestOptions.DEFAULT);
    }
}

