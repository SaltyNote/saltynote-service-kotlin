package com.saltynote.service.service

import com.google.common.annotations.VisibleForTesting
import com.saltynote.service.entity.Note
import com.saltynote.service.repository.NoteRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
@CacheConfig(cacheNames = ["note"])
class NoteService(val repository: NoteRepository) : RepositoryService<String, Note> {
    @Caching(evict = [CacheEvict(key = "#entity.userId + #entity.url"), CacheEvict(key = "#entity.userId")])
    override fun create(entity: Note): Note {
        if (hasValidId(entity)) {
            logger.warn { "Note id must be empty: $entity" }
        }
        return repository.save<Note>(entity)
    }

    @Caching(evict = [CacheEvict(key = "#entity.id"), CacheEvict(key = "#entity.userId + #entity.url"), CacheEvict(key = "#entity.userId")])
    override fun update(entity: Note): Note {
        checkIdExists(entity)
        return repository.save<Note>(entity)
    }

    @Cacheable(key = "#id")
    override fun getById(id: String): Optional<Note> {
        return repository.findById(id)
    }

    @Caching(evict = [CacheEvict(key = "#entity.id"), CacheEvict(key = "#entity.userId + #entity.url"), CacheEvict(key = "#entity.userId")])
    override fun delete(entity: Note) {
        repository.deleteById(entity.id!!)
    }

    @Cacheable(key = "#userId")
    fun getAllByUserId(userId: String): List<Note> {
        return repository.findAllByUserId(userId)
    }

    @Cacheable(key = "#userId + #url")
    fun getAllByUserIdAndUrl(userId: String, url: String): List<Note> {
        return repository.findAllByUserIdAndUrl(userId, url)
    }

    @VisibleForTesting
    @CacheEvict(allEntries = true)
    fun deleteAll(notesToCleaned: List<Note>) {
        repository.deleteAll(notesToCleaned)
    }
}
