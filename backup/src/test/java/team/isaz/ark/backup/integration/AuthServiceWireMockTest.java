package team.isaz.ark.backup.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import team.isaz.ark.backup.BackupApplication;
import team.isaz.ark.backup.repository.SnippetRepository;
import team.isaz.ark.backup.service.AuthService;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = BackupApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuthServiceWireMockTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance().build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.discovery.enabled", () -> true);
        registry.add("feign.user.name", () -> "ark-user-test");
        registry.add("spring.cloud.discovery.client.simple.instances.ark-user-test[0].uri", wiremock::baseUrl);
        registry.add("elastic-search.hosts[0]", () -> "localhost");
        registry.add("elastic-search.ports[0]", () -> 9200);
        registry.add("elastic-search.scheme", () -> "http");
    }

    @Autowired
    private AuthService authService;

    @MockBean
    private SnippetRepository snippetRepository;

    @Test
    void shouldAllowAdminToken() {
        wiremock.stubFor(get(urlPathEqualTo("/internal/bearer/check"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"OK\",\"login\":\"captain\",\"role\":\"ROLE_ADMIN\"}")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer admin");

        assertDoesNotThrow(() -> authService.checkAdmin(headers));
    }

    @Test
    void shouldRejectUserRoleToken() {
        wiremock.stubFor(get(urlPathEqualTo("/internal/bearer/check"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"OK\",\"login\":\"captain\",\"role\":\"ROLE_USER\"}")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer user");

        assertThrows(AuthenticationSin.class, () -> authService.checkAdmin(headers));
    }

    @Test
    void shouldRejectErrorStatusToken() {
        wiremock.stubFor(get(urlPathEqualTo("/internal/bearer/check"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"ERROR\",\"login\":\"captain\",\"role\":\"ROLE_ADMIN\"}")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer invalid");

        assertThrows(AuthenticationSin.class, () -> authService.checkAdmin(headers));
    }
}
