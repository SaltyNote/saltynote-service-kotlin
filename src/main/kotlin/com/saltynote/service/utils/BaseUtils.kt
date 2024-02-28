package com.saltynote.service.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.saltynote.service.domain.transfer.ServiceResponse
import com.saltynote.service.security.SecurityConstants
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

object BaseUtils {
    private var baseUrl = "https://saltynote.com"


    val objectMapper: ObjectMapper = ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .registerModule(JavaTimeModule())
        .registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.NullToEmptyCollection, true)
                .configure(KotlinFeature.NullToEmptyMap, true)
                .build()
        )    // This is used for test or dev usage, do not call it in prod.

    fun setBaseUrl(baseUrl: String) {
        if (StringUtils.startsWithIgnoreCase(baseUrl, "http")) {
            BaseUtils.baseUrl = baseUrl
        }
    }

    fun getPasswordResetUrl(secret: String): String {
        return "$baseUrl/password/reset?token=$secret"
    }

    fun containsAllIgnoreCase(src: String, queries: Iterable<String>): Boolean {
        if (StringUtils.isBlank(src)) {
            return false
        }
        for (q in queries) {
            if (StringUtils.isNotBlank(q) && !StringUtils.containsIgnoreCase(src, q.trim { it <= ' ' })) {
                return false
            }
        }
        return true
    }

    fun toJson(e: Any): String {
        return objectMapper.writeValueAsString(e)
    }

    fun buildErrorResponse(
        response: HttpServletResponse, httpStatus: HttpStatus, errorMessage: String
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = httpStatus.value()
        response.writer.write(toJson(ServiceResponse(httpStatus, errorMessage)))
    }

}
