package com.saltynote.service.repository

import com.saltynote.service.entity.Note
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepository : MongoRepository<Note, Long> {
    fun findAllByUserId(userId: Long): List<Note>
    fun findAllByUserIdAndUrl(userId: Long, url: String): List<Note>
    fun deleteByUserId(userId: Long)
}
