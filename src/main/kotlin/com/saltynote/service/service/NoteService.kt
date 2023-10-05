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
class NoteService(val repository: NoteRepository) : RepositoryService<Long, Note> {
    @Caching(evict = [CacheEvict(key = "#entity.userId + #entity.url"), CacheEvict(key = "#entity.userId")])
    override fun create(entity: Note): Note {
        return repository.save(entity)
    }

    @Caching(evict = [CacheEvict(key = "#entity.id"), CacheEvict(key = "#entity.userId + #entity.url"), CacheEvict(key = "#entity.userId")])
    override fun update(entity: Note): Note {
        return repository.save<Note>(entity)
    }

    @Cacheable(key = "#id")
    override fun getById(id: Long): Optional<Note> {
        return repository.findById(id)
    }

    @Caching(evict = [CacheEvict(key = "#entity.id"), CacheEvict(key = "#entity.userId + #entity.url"), CacheEvict(key = "#entity.userId")])
    override fun delete(entity: Note) {
        repository.deleteById(entity.getId())
    }

    @Cacheable(key = "#userId")
    fun getAllByUserId(userId: Long): List<Note> {
        return repository.findAllByUserId(userId)
    }

    @Cacheable(key = "#userId + #url")
    fun getAllByUserIdAndUrl(userId: Long, url: String): List<Note> {
        return repository.findAllByUserIdAndUrl(userId, url)
    }

    @VisibleForTesting
    @CacheEvict(allEntries = true)
    fun deleteAll(notesToCleaned: List<Note>) {
        repository.deleteAll(notesToCleaned)
    }
}
