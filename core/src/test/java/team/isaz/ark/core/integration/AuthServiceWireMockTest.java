package team.isaz.ark.core.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import team.isaz.ark.core.CoreApplication;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.core.service.AuthService;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CoreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuthServiceWireMockTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance().build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.discovery.enabled", () -> true);
        registry.add("feign.user.name", () -> "ark-user-test");
        registry.add("spring.cloud.discovery.client.simple.instances.ark-user-test[0].uri",
                () -> wiremock.baseUrl());
        registry.add("elastic-search.hosts[0]", () -> "localhost");
        registry.add("elastic-search.ports[0]", () -> 9200);
        registry.add("elastic-search.scheme", () -> "http");
    }

    @MockBean
    private SnippetRepository snippetRepository;

    @Autowired
    private AuthService authService;

    @Test
    void shouldResolveLoginUsingWireMockedUserService() {
        wiremock.stubFor(get(urlPathEqualTo("/internal/bearer/check"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"OK\",\"login\":\"captain\",\"role\":\"USER\"}")));

        String login = authService.getLogin("Bearer token");

        assertThat(login).isEqualTo("captain");
    }
}
