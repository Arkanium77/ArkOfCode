package team.isaz.ark.user.configuration.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import team.isaz.ark.user.configuration.CustomUserDetails;
import team.isaz.ark.user.service.auxiliary.CustomUserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    public static final String AUTHORIZATION = "Authorization";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.trace("do filter...");
        String token = ((HttpServletRequest) servletRequest).getHeader(AUTHORIZATION);
        if (token != null && jwtProvider.validateToken(token) && jwtProvider.isThatAccessToken(token)) {
            String userLogin = jwtProvider.getLoginFromToken(token);
            UUID verifyCode = jwtProvider.getTokenVerifyCode(token);
            CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(userLogin);
            if (!verifyCode.equals(customUserDetails.getTokenVerifyCode())) {
                log.info("Token verify code from token don't equals code received from service");
                log.debug("login: {}, corrupted verifyCode  {}", userLogin, verifyCode);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (customUserDetails.isUserBanned()) {
                log.info("User banned!");
                log.debug("login: {}, isUserBanned =  {}", userLogin, customUserDetails.isUserBanned());
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(customUserDetails, null,
                                                                                               customUserDetails
                                                                                                       .getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public boolean isTokenValid(String token) {
        if (token != null && jwtProvider.validateToken(token) && jwtProvider.isThatAccessToken(token)) {
            String userLogin = jwtProvider.getLoginFromToken(token);
            UUID verifyCode = jwtProvider.getTokenVerifyCode(token);
            CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(userLogin);
            if (!verifyCode.equals(customUserDetails.getTokenVerifyCode())) {
                log.info("Token verify code from token don't equals code received from service");
                log.debug("login: {}, corrupted verifyCode  {}", userLogin, verifyCode);
                return false;
            }
            return true;
        }
        return false;
    }

}
