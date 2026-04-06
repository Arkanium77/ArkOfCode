package team.isaz.ark.core.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.Response;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.service.AuthService;
import team.isaz.ark.core.service.InternalOperationService;
import team.isaz.ark.core.service.PublisherService;
import team.isaz.ark.core.service.SearchService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreControllersTest {

    @Mock
    private SearchService searchService;
    @Mock
    private PublisherService publisherService;
    @Mock
    private AuthService authService;
    @Mock
    private InternalOperationService internalOperationService;

    private PublicController publicController;
    private SecuredController securedController;
    private InternalController internalController;
    private ErrorStubController errorStubController;

    @BeforeEach
    void setUp() {
        publicController = new PublicController(searchService);
        securedController = new SecuredController(searchService, publisherService, authService);
        internalController = new InternalController(internalOperationService, authService);
        errorStubController = new ErrorStubController();
    }

    @Test
    void publicSearchAndGetShouldReturnOk() {
        Snippet snippet = Snippet.builder().id("snip-1").title("T").text("X").build();
        when(searchService.search("query")).thenReturn(List.of(snippet));
        when(searchService.get("snip-1")).thenReturn(snippet);

        ResponseEntity<List<Snippet>> searchResponse = publicController.search("query");
        ResponseEntity<Snippet> getResponse = publicController.get("snip-1");

        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertEquals(1, searchResponse.getBody().size());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("snip-1", getResponse.getBody().getId());
    }

    @Test
    void securedEndpointsShouldDelegateToServices() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer t");

        when(authService.getToken(headers)).thenReturn("Bearer t");
        when(authService.checkToken("Bearer t")).thenReturn(new TokenCheck(Status.OK, "user", "ROLE_USER"));

        Snippet snippet = Snippet.builder().id("snip-2").build();
        when(searchService.search("Bearer t", "q")).thenReturn(List.of(snippet));
        when(searchService.get("Bearer t", "snip-2")).thenReturn(snippet);

        Response publishResponse = Response.ok("published");
        when(publisherService.publish("Bearer t", true, "title", Set.of("tag"), "body")).thenReturn(publishResponse);

        Response updateResponse = Response.ok("updated");
        when(publisherService.update("snip-2", "Bearer t", false, "new", Set.of("tag"), "text")).thenReturn(updateResponse);

        assertEquals(HttpStatus.OK, securedController.getMyLogin(headers).getStatusCode());
        assertEquals("user", securedController.getMyLogin(headers).getBody().getLogin());
        assertEquals(1, securedController.search(headers, "q").getBody().size());
        assertEquals("snip-2", securedController.get(headers, "snip-2").getBody().getId());
        assertEquals("published", securedController.publish(headers, true, "title", Set.of("tag"), "body").getBody().getDescription());
        assertEquals("updated", securedController.update("snip-2", headers, false, "new", Set.of("tag"), "text").getBody().getDescription());
    }

    @Test
    void securedUpdateShouldConvertBlankTextToNull() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer t");
        when(authService.getToken(headers)).thenReturn("Bearer t");
        when(publisherService.update("snip-3", "Bearer t", null, null, null, null)).thenReturn(Response.ok("ok"));

        ResponseEntity<Response> response = securedController.update("snip-3", headers, null, null, null, "   ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(publisherService).update("snip-3", "Bearer t", null, null, null, null);
    }

    @Test
    void internalControllerShouldUpdateLoginAndReturnOkStatus() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer int");
        when(authService.getToken(headers)).thenReturn("Bearer int");

        ResponseEntity<Status> response = internalController.updateLogin(headers, "old", "new");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Status.OK, response.getBody());
        verify(internalOperationService).updateLogin("old", "new", "Bearer int");
    }

    @Test
    void errorStubShouldReturnForbidden() {
        ResponseEntity<String> response = errorStubController.error();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Попытка доступа к защищённому методу из внешнего контура!", response.getBody());
    }
}
