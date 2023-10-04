package com.saltynote.service.service

import com.saltynote.service.entity.LoginHistory
import com.saltynote.service.entity.SiteUser
import com.saltynote.service.repository.LoginHistoryRepository
import com.saltynote.service.repository.NoteRepository
import com.saltynote.service.repository.UserRepository
import com.saltynote.service.repository.VaultRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
@CacheConfig(cacheNames = ["user"])
class UserService(
    val repository: UserRepository,
    val noteRepository: NoteRepository,
    val vaultRepository: VaultRepository,
    val loginHistoryRepository: LoginHistoryRepository,
) : RepositoryService<String, SiteUser> {

    @Caching(put = [CachePut(key = "#entity.id"), CachePut(key = "#entity.username"), CachePut(key = "#entity.email")])
    override fun create(entity: SiteUser): SiteUser {
        if (hasValidId(entity)) {
            logger.warn { "Note id must be empty: $entity" }
        }
        return repository.save(entity)
    }

    @Caching(put = [CachePut(key = "#entity.id"), CachePut(key = "#entity.username"), CachePut(key = "#entity.email")])
    override fun update(entity: SiteUser): SiteUser {
        checkIdExists(entity)
        return repository.save(entity)
    }

    @Cacheable(key = "#id")
    override fun getById(id: String): Optional<SiteUser> {
        return repository.findById(id)
    }

    // No need to do cache evict here, since all stale content will be expired soon.
    override fun delete(entity: SiteUser) {
        repository.deleteById(entity.id!!)
    }

    // This api will delete all database records with given user id, including the user
    // itself.
    // No need to do cache evict here, since all stale content will be expired soon.
    @Transactional
    @CacheEvict(key = "#userId")
    fun cleanupByUserId(userId: String) {
        noteRepository.deleteByUserId(userId)
        vaultRepository.deleteByUserId(userId)
        loginHistoryRepository.deleteByUserId(userId)
        repository.deleteById(userId)
    }

    @Cacheable(key = "#email")
    fun getByEmail(email: String): SiteUser? {
        return repository.findByEmail(email)
    }

    @Cacheable(key = "#username")
    fun getByUsername(username: String?): SiteUser? {
        return repository.findByUsername(username!!)
    }

    fun saveLoginHistory(userId: String, ip: String?, userAgent: String?) {
        val loginHistory = LoginHistory(userId = userId, remoteIp = ip ?: "", userAgent = userAgent ?: "")
        loginHistoryRepository.save(loginHistory)
    }
}
