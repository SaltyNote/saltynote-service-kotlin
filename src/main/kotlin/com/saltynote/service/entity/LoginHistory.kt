package com.saltynote.service.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class LoginHistory(
    @Id
    val id: String? = null,
    val userId: String = "",
    val remoteIp: String = "",
    val userAgent: String = "",
    val loginTime: Long = System.currentTimeMillis(),
)