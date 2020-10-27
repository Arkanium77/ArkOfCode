package team.isaz.ark.user.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Aspect
@Component
public class TokenAspect {

    @Pointcut("@annotation(team.isaz.ark.user.aop.annotation.PrepareToken)")
    public void prepareToken() {
    }

    @Around("prepareToken()")
    public Object prepareToken(ProceedingJoinPoint jp) {
        CodeSignature methodSignature = (CodeSignature) jp.getSignature();
        String[] sigParamNames = methodSignature.getParameterNames();
        int indexOfParam = Arrays.asList(sigParamNames).indexOf("token");
        Object[] args = jp.getArgs();
        if (indexOfParam != -1) {
            args[indexOfParam] = getTokenFromHeaderRequest((String) args[indexOfParam]);
        }
        try {
            return jp.proceed(args);
        } catch (Throwable throwable) {
            log.error("Unexpected {} : {}", throwable.getClass().getSimpleName(), throwable.getMessage());
            throw new RuntimeException(throwable);
        }
    }

    private String getTokenFromHeaderRequest(String token) {
        if (hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

}
