package team.isaz.ark.user.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import team.isaz.ark.user.config.CustomUserDetails;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.service.auxiliary.CustomUserDetailsService;

import java.io.IOException;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateValidAccessToken() throws ServletException, IOException {
        JwtFilter jwtFilter = new JwtFilter(jwtProvider, customUserDetailsService);
        UUID verifyCode = UUID.randomUUID();
        CustomUserDetails details = CustomUserDetails.fromUserEntityToCustomUserDetails(UserEntity.builder()
                .login("captain")
                .password("secret")
                .tokenVerifyCode(verifyCode)
                .role(RoleEntity.builder().name("ROLE_USER").build())
                .build());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION, "Bearer token");
        Mockito.when(jwtProvider.validateToken("Bearer token")).thenReturn(true);
        Mockito.when(jwtProvider.isThatAccessToken("Bearer token")).thenReturn(true);
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(jwtProvider.getTokenVerifyCode("Bearer token")).thenReturn(verifyCode);
        Mockito.when(customUserDetailsService.loadUserByUsername("captain")).thenReturn(details);

        jwtFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        Assertions.assertThat(jwtFilter.isTokenValid("Bearer token")).isTrue();
    }

    @Test
    void shouldSkipInvalidAccessToken() throws ServletException, IOException {
        JwtFilter jwtFilter = new JwtFilter(jwtProvider, customUserDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION, "Bearer token");
        Mockito.when(jwtProvider.validateToken("Bearer token")).thenReturn(false);

        jwtFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        Assertions.assertThat(jwtFilter.isTokenValid("Bearer token")).isFalse();
    }

    @Test
    void shouldSkipRefreshToken() throws ServletException, IOException {
        JwtFilter jwtFilter = new JwtFilter(jwtProvider, customUserDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION, "Bearer token");
        Mockito.when(jwtProvider.validateToken("Bearer token")).thenReturn(true);
        Mockito.when(jwtProvider.isThatAccessToken("Bearer token")).thenReturn(false);

        jwtFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldRejectMismatchedVerifyCode() {
        JwtFilter jwtFilter = new JwtFilter(jwtProvider, customUserDetailsService);
        UUID verifyCode = UUID.randomUUID();
        UUID anotherCode = UUID.randomUUID();
        CustomUserDetails details = CustomUserDetails.fromUserEntityToCustomUserDetails(UserEntity.builder()
                .login("captain")
                .tokenVerifyCode(anotherCode)
                .role(RoleEntity.builder().name("ROLE_USER").build())
                .build());
        Mockito.when(jwtProvider.validateToken("Bearer token")).thenReturn(true);
        Mockito.when(jwtProvider.isThatAccessToken("Bearer token")).thenReturn(true);
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(jwtProvider.getTokenVerifyCode("Bearer token")).thenReturn(verifyCode);
        Mockito.when(customUserDetailsService.loadUserByUsername("captain")).thenReturn(details);

        Assertions.assertThat(jwtFilter.isTokenValid("Bearer token")).isFalse();
    }

    @Test
    void shouldSkipBannedUser() throws ServletException, IOException {
        JwtFilter jwtFilter = new JwtFilter(jwtProvider, customUserDetailsService);
        UUID verifyCode = UUID.randomUUID();
        CustomUserDetails details = CustomUserDetails.fromUserEntityToCustomUserDetails(UserEntity.builder()
                .login("captain")
                .tokenVerifyCode(verifyCode)
                .role(RoleEntity.builder().name("ROLE_USER").build())
                .build()
                .withUserBanned(true));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION, "Bearer token");
        Mockito.when(jwtProvider.validateToken("Bearer token")).thenReturn(true);
        Mockito.when(jwtProvider.isThatAccessToken("Bearer token")).thenReturn(true);
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(jwtProvider.getTokenVerifyCode("Bearer token")).thenReturn(verifyCode);
        Mockito.when(customUserDetailsService.loadUserByUsername("captain")).thenReturn(details);

        jwtFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
