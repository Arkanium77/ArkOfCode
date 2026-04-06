package team.isaz.ark.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import team.isaz.ark.user.configuration.jwt.JwtFilter;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.constants.Status;
import team.isaz.ark.user.dto.TokenCheck;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.service.main.AccountService;
import team.isaz.ark.user.service.main.AdminService;
import team.isaz.ark.user.service.main.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllersTest {

    @Mock
    private AccountService accountService;
    @Mock
    private UserService userService;
    @Mock
    private AdminService adminService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private JwtFilter jwtFilter;

    private AuthController authController;
    private UserController userController;
    private AdminController adminController;
    private InternalController internalController;
    private ErrorStubController errorStubController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(accountService);
        userController = new UserController(userService);
        adminController = new AdminController(adminService);
        internalController = new InternalController(jwtProvider, jwtFilter);
        errorStubController = new ErrorStubController();
    }

    @Test
    void authControllerShouldCoverRegisterAuthAndRefreshBranches() {
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("tester_1");
        userInfo.setPassword("pass123");

        when(accountService.registerUser(userInfo)).thenReturn(UserEntity.builder().login("tester_1").build());
        Tokens tokens = Tokens.builder().accessToken("a").refreshToken("r").build();
        when(accountService.login(userInfo)).thenReturn(tokens);
        when(accountService.refreshToken("refresh")).thenReturn(tokens);

        ResponseEntity<String> registerResponse = authController.registerUser(userInfo);
        ResponseEntity<?> authOkResponse = authController.auth(userInfo);
        ResponseEntity<Tokens> refreshResponse = authController.registerUser("refresh");

        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertEquals(HttpStatus.OK, authOkResponse.getStatusCode());
        assertInstanceOf(Tokens.class, authOkResponse.getBody());
        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());

        when(accountService.login(userInfo)).thenReturn(null);
        ResponseEntity<?> authFailResponse = authController.auth(userInfo);
        assertEquals(HttpStatus.UNAUTHORIZED, authFailResponse.getStatusCode());
        assertEquals("Login failed! Try again!", authFailResponse.getBody());
    }

    @Test
    void userControllerShouldHandleAccountActions() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer t");

        ResponseEntity<String> loginResponse = userController.changeLogin("new_login", headers);
        ResponseEntity<String> passResponse = userController.changePassword("newPass123", headers);
        ResponseEntity<String> deleteResponse = userController.deleteAccount(headers);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertEquals(HttpStatus.OK, passResponse.getStatusCode());
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        verify(userService).changeLogin("Bearer t", "new_login");
        verify(userService).changePassword("Bearer t", "newPass123");
        verify(userService).deleteAccount("Bearer t");
    }

    @Test
    void adminControllerShouldDelegateAllActions() {
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("new_user");
        userInfo.setPassword("new_pass");

        when(adminService.getId("login_1")).thenReturn(10L);

        assertEquals("10", adminController.delete("login_1").getBody());
        assertEquals(HttpStatus.OK, adminController.ban(1L).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.unBan(2L).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.delete(3L).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.promote(4L).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.demote(5L).getStatusCode());
        assertEquals(HttpStatus.OK, adminController.update(6L, userInfo).getStatusCode());

        verify(adminService).banAccount(1L);
        verify(adminService).unBanAccount(2L);
        verify(adminService).deleteAccount(3L);
        verify(adminService).promoteAccount(4L);
        verify(adminService).demoteAccount(5L);
        verify(adminService).changeUserData(6L, userInfo);
    }

    @Test
    void internalControllerShouldCoverBothTokenBranches() {
        TokenCheck ok = TokenCheck.builder().status(Status.OK).login("u").role("ROLE_USER").build();
        when(jwtFilter.isTokenValid("valid")).thenReturn(true);
        when(jwtProvider.getInfoFromToken("valid")).thenReturn(ok);

        TokenCheck validResult = internalController.check("valid");
        TokenCheck invalidResult = internalController.check("invalid");

        assertEquals(Status.OK, validResult.getStatus());
        assertEquals("u", validResult.getLogin());
        assertEquals(Status.ERROR, invalidResult.getStatus());
    }

    @Test
    void errorStubShouldReturnForbidden() {
        ResponseEntity<String> response = errorStubController.error();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Попытка доступа к защищённому методу из внешнего контура!", response.getBody());
    }
}
