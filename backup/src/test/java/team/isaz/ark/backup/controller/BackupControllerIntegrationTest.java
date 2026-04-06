package team.isaz.ark.backup.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import team.isaz.ark.backup.config.BasicIntegrationTest;
import team.isaz.ark.backup.entity.Snippet;
import team.isaz.ark.backup.support.TestDirectorySupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

class BackupControllerIntegrationTest extends BasicIntegrationTest {

    @Test
    void shouldBackupAndRestoreThroughController() throws Exception {
        stubAdminCheck();
        snippetRepository.save(Snippet.builder().id("s-1").author("captain").title("hello").text("world").tags(Set.of("java")).build());
        snippetRepository.save(Snippet.builder().id("s-2").author("captain").title("hi").text("earth").tags(Set.of("spring")).build());
        refreshIndex();

        Path tempDir = TestDirectorySupport.create("backup-controller-");
        try {
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
        } finally {
            TestDirectorySupport.delete(tempDir);
        }
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
}



