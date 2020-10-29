package team.isaz.ark.user.aop;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(team.isaz.ark.user.service.main.*)")
    public void debugLog() {
    }

    @SneakyThrows
    @Around("debugLog()")
    public Object logIt(ProceedingJoinPoint jp) {
        log.trace("\u001B[35m===== LOGGING ASPECT IN=====\u001B[0m");
        CodeSignature methodSignature = (CodeSignature) jp.getSignature();
        StringBuilder builder = new StringBuilder("Method ").append(methodSignature.getName())
                .append(" run with args:");

        Object[] args = jp.getArgs();
        List<String> sigParamNames = Arrays.asList(methodSignature.getParameterNames());
        for (int i = 0; i < sigParamNames.size(); i++) {
            builder.append("\n\t").append(sigParamNames.get(i))
                    .append(" = ")
                    .append(args[i]);
        }
        log.debug("{}", builder.toString());
        try {
            Object o = jp.proceed(args);
            log.debug("Method {} successful return \n {}", methodSignature.getName(), o);
            log.trace("\u001B[35m===== ASPECT OUT =====\u001B[0m");
            return o;
        } catch (Throwable throwable) {
            log.error("Unexpected {} : {}", throwable.getClass().getSimpleName(), throwable.getMessage());
            log.trace("\u001B[35m===== ASPECT OUT =====\u001B[0m");
            throw throwable;
        }
    }

}
