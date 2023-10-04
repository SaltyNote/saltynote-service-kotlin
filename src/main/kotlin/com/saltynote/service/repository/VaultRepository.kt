package com.saltynote.service.repository

import com.saltynote.service.entity.Vault
import jakarta.validation.constraints.NotBlank
import org.springframework.data.mongodb.repository.MongoRepository

interface VaultRepository : MongoRepository<Vault, Long> {
    fun findBySecret(secret: String): Vault?
    fun deleteByUserId(userId: Long)
    fun deleteByUserIdAndType(userId: Long, type: String)
    fun findByUserIdAndTypeAndSecret(userId: Long, type: String, secret: String): Vault?
    fun findByUserIdAndType(userId: Long, type: String): List<Vault>
    fun findFirstByUserIdAndTypeOrderByCreatedTimeDesc(userId: Long, type: String): Vault?
    fun findByUserId(userId: Long): List<Vault>
    fun findByEmail(email: @NotBlank String): List<Vault>
    fun findByEmailAndSecretAndType(email: String, token: String, value: String): Vault?
}
