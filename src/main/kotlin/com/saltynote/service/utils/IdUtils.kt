package com.saltynote.service.utils

import com.saltynote.service.security.SecurityConstants
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

object IdUtils {

    private const val BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LENGTH = BASE62.length

    fun toString(number: Long): String {
        var num = number
        if (num == 0L) return "0"

        val base62String = StringBuilder()
        while (num > 0) {
            val remainder = (num % LENGTH).toInt()
            base62String.insert(0, BASE62[remainder])
            num /= LENGTH
        }

        return base62String.toString()
    }

    fun toNumber(str: String): Long {
        var number = 0L
        str.forEach { char ->
            number = number * LENGTH + BASE62.indexOf(char)
        }
        return number
    }

    fun getUserId(): Long {
        return RequestContextHolder.currentRequestAttributes().getAttribute(
            SecurityConstants.CLAIM_KEY_USER_ID,
            RequestAttributes.SCOPE_REQUEST
        ) as Long
    }
}
