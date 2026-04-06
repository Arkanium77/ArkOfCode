package team.isaz.ark.backup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import team.isaz.ark.backup.BackupApplication;
import team.isaz.ark.backup.config.initializer.ElasticsearchInitializer;
import team.isaz.ark.backup.repository.SnippetRepository;

@SpringBootTest(classes = BackupApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = ElasticsearchInitializer.class)
@ActiveProfiles("test")
public abstract class BasicIntegrationTest {
    protected static final int USER_SERVICE_PORT = 10103;

    @RegisterExtension
    protected static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(USER_SERVICE_PORT))
            .build();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected SnippetRepository snippetRepository;

    @Autowired
    protected RestHighLevelClient restHighLevelClient;

    @Autowired
    protected ObjectMapper objectMapper;

    @AfterEach
    void cleanUp() throws Exception {
        snippetRepository.deleteAll();
        refreshIndex();
        wireMock.resetAll();
    }

    protected void refreshIndex() throws Exception {
        restHighLevelClient.indices().refresh(new RefreshRequest("snippets"), RequestOptions.DEFAULT);
    }
}
