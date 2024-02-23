package com.saltynote.service.utils

import org.apache.commons.lang3.StringUtils

object BaseUtils {
    private var baseUrl = "https://saltynote.com"

    // This is used for test or dev usage, do not call it in prod.
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
}
