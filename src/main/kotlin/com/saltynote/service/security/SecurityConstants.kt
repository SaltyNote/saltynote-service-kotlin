package com.saltynote.service.security

import com.saltynote.service.exception.IllegalInitialException

class SecurityConstants private constructor() {
    init {
        throw IllegalInitialException("Do not instantiate me.")
    }

    companion object {
        const val TOKEN_PREFIX = "Bearer "
        const val AUTH_HEADER = "Authorization"
        const val USER_AGENT_HEADER = "User-Agent"
        const val REAL_IP_HEADER = "X-Real-IP"
        const val SALTY_PORT_HEADER = "X-SaltyNote-Port"
        const val SIGN_UP_URL = "/signup"
        const val CLAIM_KEY_USER_ID = "user_id"
    }
}
