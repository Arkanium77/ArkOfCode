package team.isaz.ark.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import team.isaz.ark.core.entity.Snippet;

import java.time.OffsetDateTime;

@Slf4j
@Aspect
@Component
public class SnippetPreprocessingAspect {

    @Pointcut("within(team.isaz.ark.core.service.PublisherService) && execution(* *..publish(team.isaz.ark.core.entity.Snippet))")
    public void prepareSnippet() {
    }

    @Around("prepareSnippet()")
    public Object prepareSnippet(ProceedingJoinPoint jp) {
        log.info("SNIPPET PREPARING");
        Object[] args = jp.getArgs();
        args[0] = setup((Snippet) args[0]);
        try {
            return jp.proceed(args);
        } catch (Throwable throwable) {
            log.error("Unexpected {} : {}", throwable.getClass().getSimpleName(), throwable.getMessage());
            throw new RuntimeException(throwable);
        }
    }

    public Snippet setup(Snippet s) {
        log.info("{}", s);
        if (s.getCreateDttm() == null) {
            s.setCreateDttm(OffsetDateTime.now());
        }
        if (s.getModifyDttm() == null) {
            s.setModifyDttm(OffsetDateTime.now());
        }
        log.info("{}", s);
        return s;
    }
}
