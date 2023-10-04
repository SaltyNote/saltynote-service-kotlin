package com.saltynote.service.entity

import com.saltynote.service.domain.IdentifiableUser
import com.saltynote.service.generator.SnowflakeIdGenerator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id
    val id: Long = SnowflakeIdGenerator.DEFAULT.nextId(),
    val username: String = "",
    val email: String = "",
    var password: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) : IdentifiableUser {

    override fun getUsername() = username

    override fun getId() = id
}
