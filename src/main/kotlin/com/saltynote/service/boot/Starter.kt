package com.saltynote.service.boot

import com.saltynote.service.utils.BaseUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@Profile("dev", "test", "local")
class Starter : CommandLineRunner {
    @Value("\${server.port}")
    private var port = 0

    /**
     * Inject the local server into email content.
     */
    override fun run(vararg args: String) {
        val baseUrl = "http://127.0.0.1:$port"
        BaseUtils.setBaseUrl(baseUrl)
        logger.info { "Set base url to $baseUrl" }
    }
}
