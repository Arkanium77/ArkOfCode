package team.isaz.ark.user.configuration.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import team.isaz.ark.user.configuration.CustomUserDetails;
import team.isaz.ark.user.service.auxiliary.CustomUserDetailsService;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class JwtFilter extends GenericFilterBean {

    public static final String AUTHORIZATION = "Authorization";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtFilter(JwtProvider jwtProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtProvider = jwtProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

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
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
