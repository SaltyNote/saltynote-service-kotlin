package com.saltynote.service.service

import com.auth0.jwt.exceptions.JWTVerificationException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.saltynote.service.domain.IdentifiableUser
import com.saltynote.service.domain.VaultEntity
import com.saltynote.service.domain.VaultType
import com.saltynote.service.entity.Vault
import com.saltynote.service.repository.VaultRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class VaultService(
    val repository: VaultRepository,
    val objectMapper: ObjectMapper,
    val jwtService: JwtService,
) : RepositoryService<String, Vault> {

    // TTL in milliseconds
    @Value("\${jwt.refresh_token.ttl}")
    private var refreshTokenTTL: Long = 0

    fun create(userId: String, type: VaultType): Vault {
        return create(userId, type, RandomStringUtils.randomAlphanumeric(8))
    }

    fun createVerificationCode(email: String): Vault {
        return repository.save(
            Vault(email = email, type = VaultType.NEW_ACCOUNT.value, secret = RandomStringUtils.randomNumeric(6))
        )
    }

    private fun create(userId: String?, type: VaultType, secret: String): Vault {
        return repository.save(Vault(userId = userId, type = type.value, secret = secret))
    }

    @Throws(JsonProcessingException::class)
    fun encode(entity: VaultEntity?): String {
        return Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(entity))
    }

    @Throws(JsonProcessingException::class)
    fun encode(vault: Vault): String {
        return encode(VaultEntity.from(vault))
    }

    fun decode(encodedValue: String): VaultEntity? {
        return try {
            objectMapper.readValue(Base64.getDecoder().decode(encodedValue), VaultEntity::class.java)
        } catch (e: IOException) {
            logger.error { "${e.message}, $e" }
            return null
        }
    }

    fun createRefreshToken(user: IdentifiableUser): String {
        val refreshToken = jwtService.createRefreshToken(user)
        val (_, _, secret) = create(user.getId(), VaultType.REFRESH_TOKEN, refreshToken)
        return secret!!
    }

    /**
     * This is try to find the latest refresh token for given user id. If the refresh
     * token ages below 20%, we will return this refresh token. Otherwise, a new refresh
     * token will be generated and returned.
     *
     * @param user the target user
     * @return the refresh token value
     */
    fun fetchOrCreateRefreshToken(user: IdentifiableUser): String {
        val vault = repository.findFirstByUserIdAndTypeOrderByCreatedTimeDesc(
            user.getId()!!,
            VaultType.REFRESH_TOKEN.value
        )
        // If refresh token is young enough, then just return it.
        return if (vault != null && isRefreshTokenReusable(vault.secret)) {
            vault.secret!!
        } else createRefreshToken(user)
        // Refresh token is not a kid anymore or no existing refresh token found, a new
        // one should be created.
    }

    /**
     * Validate given token and return the vault.
     *
     * @param token token
     * @return vault for the token
     */
    fun findByToken(token: String): Vault? {
        val veo = decode(token)
        veo?.let {
            val (userId, secret) = it
            val vault = repository.findBySecret(secret)
            if (vault == null || vault.userId != userId) {
                logger.error { "User id are not match from decoded token $userId and database ${vault?.userId}" }
                return null
            }
            return vault
        }
        return null
    }

    override fun create(entity: Vault): Vault {
        if (hasValidId(entity)) {
            logger.warn { "Note id must be empty: $entity" }
        }
        return repository.save(entity)
    }

    override fun update(entity: Vault): Vault {
        checkIdExists(entity)
        return repository.save(entity)
    }

    override fun getById(id: String): Optional<Vault> {
        return repository.findById(id)
    }

    override fun delete(entity: Vault) {
        repository.deleteById(entity.id!!)
    }

    fun deleteById(id: String) {
        repository.deleteById(id)
    }

    fun findByUserIdAndTypeAndValue(userId: String, type: VaultType, secret: String): Vault? {
        return repository.findByUserIdAndTypeAndSecret(userId, type.value, secret)
    }

    fun cleanRefreshTokenByUserId(userId: String?) {
        repository.deleteByUserIdAndType(userId!!, VaultType.REFRESH_TOKEN.value)
    }

    private fun isRefreshTokenReusable(refreshToken: String?): Boolean {
        return try {
            val decodedJWT = jwtService.verifyRefreshToken(refreshToken)
            decodedJWT.expiresAt.after(Date(System.currentTimeMillis() + refreshTokenTTL * 8 / 10))
        } catch (e: JWTVerificationException) {
            false
        }
    }

    fun getByEmailAndSecretAndType(email: String?, token: String?, type: VaultType): Vault? {
        return repository.findByEmailAndSecretAndType(email!!, token!!, type.value)
    }

    fun getByEmail(email: String?): List<Vault> {
        return repository.findByEmail(email!!)
    }

    fun getByUserIdAndType(userId: String?, vaultType: VaultType): List<Vault> {
        return repository.findByUserIdAndType(userId!!, vaultType.value)
    }

    fun getByUserId(userId: String): List<Vault> {
        return repository.findByUserId(userId)
    }
}
