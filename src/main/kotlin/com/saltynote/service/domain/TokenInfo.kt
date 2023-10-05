package com.saltynote.service.domain

import java.util.Date

data class TokenInfo(
    val loginId: Long,
    val loginType: String,
    val expireAt: Date,
    val device: String,
)
