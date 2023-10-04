package com.saltynote.service.entity

import com.saltynote.service.generator.SnowflakeIdGenerator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class LoginHistory(
    @Id
    val id: Long = SnowflakeIdGenerator.DEFAULT.nextId(),
    val userId: Long,
    val remoteIp: String = "",
    val userAgent: String = "",
    val loginTime: Long = System.currentTimeMillis(),
)