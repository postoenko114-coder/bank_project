package com.alex.bank.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("within(com.alex.bank.controllers..*) || within(com.alex.bank.services..*)")
    public Object logControllerAndServiceCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        String layer = joinPoint.getSignature().getDeclaringTypeName().contains(".controllers.") ? "controller" : "service";
        String method = joinPoint.getSignature().toShortString();
        StopWatch stopWatch = new StopWatch();

        log.debug("{} call started: {}", layer, method);
        stopWatch.start();
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.debug("{} call completed: {} in {} ms", layer, method, stopWatch.getTotalTimeMillis());
            return result;
        } catch (ResponseStatusException ex) {
            stopWatch.stop();
            log.warn("{} call failed: {} status={} reason={} duration={} ms",
                    layer, method, ex.getStatusCode(), ex.getReason(), stopWatch.getTotalTimeMillis());
            throw ex;
        } catch (Exception ex) {
            stopWatch.stop();
            log.error("{} call failed: {} duration={} ms", layer, method, stopWatch.getTotalTimeMillis(), ex);
            throw ex;
        }
    }
}
