package team.isaz.ark.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.libs.sinsystem.model.sin.InternalSin;

import java.time.OffsetDateTime;

@Slf4j
@Aspect
@Component
public class SnippetPreprocessingAspect {

    @Pointcut("@annotation(team.isaz.ark.core.aop.annotation.PrepareSnippet)")
    public void prepareSnippet() {
    }

    @Around("prepareSnippet()")
    public Object prepareSnippet(ProceedingJoinPoint jp) {
        log.info("SNIPPET PREPARING");
        Object[] args = jp.getArgs();
        setup((Snippet) args[0]);
        try {
            return jp.proceed(args);
        } catch (Throwable throwable) {
            log.error("Unexpected {} : {}", throwable.getClass()
                    .getSimpleName(), throwable.getMessage());
            throw new InternalSin(throwable);
        }
    }

    private void setup(Snippet s) {
        OffsetDateTime now = OffsetDateTime.now();
        log.info("{}", s);
        if (s.getCreateDttm() == null) {
            s.setCreateDttm(now);
        }
        s.setModifyDttm(now);
        log.info("{}", s);
    }
}
