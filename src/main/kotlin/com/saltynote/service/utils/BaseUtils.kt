package com.saltynote.service.utils

import com.saltynote.service.exception.IllegalInitialException
import jakarta.validation.constraints.NotNull
import org.apache.commons.lang3.StringUtils

class BaseUtils private constructor() {
    init {
        throw IllegalInitialException("Do not instantiate me.")
    }

    companion object {
        private var baseUrl = "https://saltynote.com"

        // This is used for test or dev usage, do not call it in prod.
        fun setBaseUrl(baseUrl: String) {
            if (StringUtils.startsWithIgnoreCase(baseUrl, "http")) {
                Companion.baseUrl = baseUrl
            }
        }

        fun getPasswordResetUrl(secret: @NotNull String?): String {
            return "$baseUrl/password/reset?token=$secret"
        }

        fun containsAllIgnoreCase(src: String?, queries: Iterable<String>): Boolean {
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
    }
}
