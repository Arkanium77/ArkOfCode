package team.isaz.ark.backup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.assertj.core.api.Assertions;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import team.isaz.ark.backup.BackupApplication;
import team.isaz.ark.backup.entity.Snippet;
import team.isaz.ark.backup.repository.SnippetRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Testcontainers
@SpringBootTest(classes = BackupApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class BackupControllerIntegrationTest {

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
    void shouldBackupAndRestoreThroughController(@TempDir Path tempDir) throws Exception {
        stubAdminCheck();
        snippetRepository.save(Snippet.builder().id("s-1").author("captain").title("hello").text("world").tags(Set.of("java")).build());
        snippetRepository.save(Snippet.builder().id("s-2").author("captain").title("hi").text("earth").tags(Set.of("spring")).build());
        refreshIndex();

        Path backupDir = tempDir.resolve("backup-store");
        MvcResult backupResult = mockMvc.perform(MockMvcRequestBuilders.post("/secured/backup")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin")
                        .param("Путь к папке резервного копирования", backupDir.toString()))
                .andReturn();

        Assertions.assertThat(backupResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(backupResult.getResponse().getContentAsString()).contains("Backup complete");
        Assertions.assertThat(Files.list(backupDir)).isNotEmpty();

        snippetRepository.deleteAll();
        refreshIndex();

        MvcResult restoreResult = mockMvc.perform(MockMvcRequestBuilders.post("/secured/restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin")
                        .param("Путь к папке резервного копирования", backupDir.toString()))
                .andReturn();

        Assertions.assertThat(restoreResult.getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(restoreResult.getResponse().getContentAsString()).contains("Successfully restored 2 snippets");
        Assertions.assertThat(snippetRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturnForbiddenStubMessage() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/public/error")).andReturn();

        Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(403);
        Assertions.assertThat(result.getResponse().getContentAsString())
                .isEqualTo("Попытка доступа к защищённому методу из внешнего контура!");
    }

    private void stubAdminCheck() {
        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/internal/bearer/check"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("{\"status\":\"OK\",\"login\":\"root\",\"role\":\"ROLE_ADMIN\"}")));
    }

    private void refreshIndex() throws Exception {
        restHighLevelClient.indices().refresh(new RefreshRequest("snippets"), RequestOptions.DEFAULT);
    }
}



