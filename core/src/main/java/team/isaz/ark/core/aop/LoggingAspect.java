package team.isaz.ark.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    private final int MAX_LENGTH = 90;

    @Pointcut("within(team.isaz.ark.core.service..*)")
    public void debugLog() {
    }

    @AfterThrowing(value = "debugLog()", throwing = "t")
    public void afterThrow(JoinPoint jp, Throwable t) {
        CodeSignature methodSignature = (CodeSignature) jp.getSignature();
        log.error("{} throws unexpected {} : {}",
                methodSignature.getName(), t.getClass().getSimpleName(), t.getMessage());
    }

    @AfterReturning(value = "debugLog()", returning = "result")
    public void afterReturn(JoinPoint jp, Object result) {
        CodeSignature methodSignature = (CodeSignature) jp.getSignature();
        if (((MethodSignature) jp.getSignature()).getReturnType().toString().equals("void")) {
            log.debug("The method {} has correctly completed the work.", methodSignature.getName());
        } else {
            if (result != null) {
                if (result.toString().length() < MAX_LENGTH) {
                    log.debug("The method {} has correctly completed the work and return:\n\t- {}",
                            methodSignature.getName(), result);
                } else {
                    log.debug("The method {} has correctly completed the work and return {}",
                            methodSignature.getName(), result.getClass().getSimpleName());

                }

            } else {
                log.debug("The method {} has correctly completed the work and return NULL value",
                        methodSignature.getName());
            }
        }
    }

    @Before("debugLog()")
    public void before(JoinPoint jp) {
        CodeSignature methodSignature = (CodeSignature) jp.getSignature();
        StringBuilder builder;
        if (jp.getArgs().length == 0) {
            builder = new StringBuilder("Method ").append(methodSignature.getName())
                    .append(" run with no args");
        } else {
            builder = new StringBuilder("Method ").append(methodSignature.getName())
                    .append(" run with args:");

            Object[] args = jp.getArgs();
            List<String> sigParamNames = Arrays.asList(methodSignature.getParameterNames());
            for (int i = 0; i < sigParamNames.size(); i++) {
                builder.append("\n\t").append(sigParamNames.get(i))
                        .append(" = ")
                        .append(args[i] != null
                                && args[i].toString().length() > MAX_LENGTH ?
                                args[i].getClass().getSimpleName() + " {...}"
                                : args[i]);
            }
        }
        log.debug("{}", builder.toString());
    }
}
