package com.saltynote.service.entity

import com.saltynote.service.domain.Identifiable
import com.saltynote.service.generator.SnowflakeIdGenerator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Vault(
    @Id
    val id: Long = SnowflakeIdGenerator.DEFAULT.nextId(),
    val userId: String,
    val secret: String,
    val type: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) : Identifiable {

    override fun getId() = id
}
