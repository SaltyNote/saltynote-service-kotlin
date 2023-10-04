package com.saltynote.service.filter

import com.saltynote.service.security.SecurityConstants
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

private val logger = KotlinLogging.logger {}

@Component
class GlobalFilter : OncePerRequestFilter(), ApplicationListener<ServletWebServerInitializedEvent> {
    private var port = 0
    override fun onApplicationEvent(event: ServletWebServerInitializedEvent) {
        port = event.webServer.port
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        response.addHeader(SecurityConstants.SALTY_PORT_HEADER, port.toString())
        filterChain.doFilter(request, response)
    }
}
