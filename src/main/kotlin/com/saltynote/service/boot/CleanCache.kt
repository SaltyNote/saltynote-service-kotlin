package com.saltynote.service.boot

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

@Component
@Profile("dev", "local", "default")
class CleanCache(val cacheManager: CacheManager) : CommandLineRunner {
    override fun run(vararg args: String) {
        cacheManager.cacheNames.forEach(Consumer { name: String? ->
            logger.info { "Clearing cache: $name" }
            val cache = cacheManager.getCache(name!!)
            cache?.clear()
        })
    }
}
