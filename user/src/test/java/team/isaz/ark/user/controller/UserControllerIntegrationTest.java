package team.isaz.ark.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.assertj.core.api.Assertions;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import team.isaz.ark.user.UserApplication;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.UserEntityRepository;

import java.util.stream.StreamSupport;

@Testcontainers
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_service")
            .withUsername("noah")
            .withPassword("righteous");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.discovery.enabled", () -> true);
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
        registry.add("feign.core.name", () -> "ark-core-test");
        registry.add("spring.cloud.discovery.client.simple.instances.ark-core-test[0].uri", wireMock::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserEntityRepository userEntityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @AfterEach
    void cleanUp() {
        StreamSupport.stream(userEntityRepository.findAll().spliterator(), false)
                .map(UserEntity::getLogin)
                .filter(login -> !"root".equals(login) && !"_tech".equals(login))
                .forEach(login -> userEntityRepository.findByLogin(login).ifPresent(userEntityRepository::delete));
        wireMock.resetAll();
    }

    @Test
    void shouldHandlePublicSecuredHiddenInternalAndErrorFlows() throws Exception {
        stubCoreUpdateLogin();
        registerUser("captain_1", "secret_1");

        Tokens userTokens = authorize("captain_1", "secret_1");
        Tokens adminTokens = authorize("root", "password");

        registerUser("captain_delete", "secret_delete");
        Long deletableUserId = getUserId("captain_delete", adminTokens);
        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.delete("/hidden/{id}/delete", deletableUserId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(authorizeForFailure("captain_delete", "secret_delete")).isEqualTo(401);

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.put("/secured/change_login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userTokens.getAccessToken())
                        .param("newLogin", "captain_2"))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        Tokens changedLoginTokens = authorize("captain_2", "secret_1");

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.put("/secured/change_password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + changedLoginTokens.getAccessToken())
                        .param("newPassword", "secret_2"))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        Tokens changedPasswordTokens = authorize("captain_2", "secret_2");

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/internal/bearer/check")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + changedPasswordTokens.getAccessToken())
                        .param("bearerToken", "Bearer " + changedPasswordTokens.getAccessToken()))
                .andReturn().getResponse().getContentAsString()).contains("\"status\":\"OK\"");

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/internal/bearer/check")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken())
                        .param("bearerToken", "Bearer broken"))
                .andReturn().getResponse().getContentAsString()).contains("\"status\":\"ERROR\"");

        Long userId = getUserId("captain_2", adminTokens);

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.put("/hidden/{id}/ban", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(authorizeForFailure("captain_2", "secret_2")).isEqualTo(401);

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.put("/hidden/{id}/unban", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.put("/hidden/{id}/promote", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.put("/hidden/{id}/demote", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        UserInfo updatedInfo = new UserInfo();
        updatedInfo.setLogin("captain_3");
        updatedInfo.setPassword("secret_3");
        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.put("/hidden/{id}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedInfo)))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        Tokens updatedTokens = authorize("captain_3", "secret_3");
        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/public/refresh")
                        .param("refreshToken", updatedTokens.getRefreshToken()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.delete("/secured/delete_account")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + updatedTokens.getAccessToken()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        Assertions.assertThat(authorizeForFailure("captain_3", "secret_3")).isEqualTo(401);

        Assertions.assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/public/error"))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
    }

    private void registerUser(String login, String password) throws Exception {
        MvcResult registerResult = mockMvc.perform(MockMvcRequestBuilders.post("/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserInfo(login, password))))
                .andReturn();
        Assertions.assertThat(registerResult.getResponse().getStatus()).isEqualTo(200);
    }

    private Long getUserId(String login, Tokens adminTokens) throws Exception {
        MvcResult getIdResult = mockMvc.perform(MockMvcRequestBuilders.get("/hidden/id/{login}", login)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.getAccessToken()))
                .andReturn();
        Assertions.assertThat(getIdResult.getResponse().getStatus()).isEqualTo(200);
        return Long.parseLong(getIdResult.getResponse().getContentAsString());
    }

    private Tokens authorize(String login, String password) throws Exception {
        MvcResult authResult = mockMvc.perform(MockMvcRequestBuilders.post("/public/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserInfo(login, password))))
                .andReturn();
        Assertions.assertThat(authResult.getResponse().getStatus()).isEqualTo(200);
        return objectMapper.readValue(authResult.getResponse().getContentAsString(), Tokens.class);
    }

    private int authorizeForFailure(String login, String password) throws Exception {
        MvcResult authResult = mockMvc.perform(MockMvcRequestBuilders.post("/public/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserInfo(login, password))))
                .andReturn();
        return authResult.getResponse().getStatus();
    }

    private UserInfo createUserInfo(String login, String password) {
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin(login);
        userInfo.setPassword(password);
        return userInfo;
    }

    private void stubCoreUpdateLogin() {
        wireMock.stubFor(WireMock.put(WireMock.urlPathEqualTo("/internal/snippets/login"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("\"OK\"")));
    }
}
