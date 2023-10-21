package com.saltynote.service.domain

data class TokenInfo(
    val loginId: Long,
    val loginType: String,
    val expireAt: Long,
    val device: String,
)
