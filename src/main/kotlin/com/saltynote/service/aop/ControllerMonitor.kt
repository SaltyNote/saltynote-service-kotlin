package com.saltynote.service.aop


import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class ControllerMonitor {
    @Pointcut("execution(* com.saltynote.service.controller.*Controller.*(..))")
    fun monitor() {
        // utility method for aop
    }

    @Around("monitor()")
    @Throws(Throwable::class)
    fun logServiceAccess(pjp: ProceedingJoinPoint): Any {
        val start = System.currentTimeMillis()
        val output: Any = pjp.proceed()
        val elapsedTime = System.currentTimeMillis() - start
        val target: String = pjp.signature.declaringType.getSimpleName() + "_" + pjp.signature.name
        logger.info { "$target execution time: $elapsedTime milliseconds." }
        return output
    }
}
