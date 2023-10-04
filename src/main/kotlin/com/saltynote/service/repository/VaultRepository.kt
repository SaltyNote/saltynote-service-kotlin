package com.saltynote.service.repository

import com.saltynote.service.entity.Vault
import jakarta.validation.constraints.NotBlank
import org.springframework.data.mongodb.repository.MongoRepository

interface VaultRepository : MongoRepository<Vault, String> {
    fun findBySecret(secret: String): Vault?
    fun deleteByUserId(userId: String)
    fun deleteByUserIdAndType(userId: String, type: String)
    fun findByUserIdAndTypeAndSecret(userId: String, type: String, secret: String): Vault?
    fun findByUserIdAndType(userId: String, type: String): List<Vault>
    fun findFirstByUserIdAndTypeOrderByCreatedTimeDesc(userId: String, type: String): Vault?
    fun findByUserId(userId: String): List<Vault>
    fun findByEmail(email: @NotBlank String): List<Vault>
    fun findByEmailAndSecretAndType(email: String, token: String, value: String): Vault?
}
