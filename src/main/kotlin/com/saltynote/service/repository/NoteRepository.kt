package com.saltynote.service.repository

import com.saltynote.service.entity.Note
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepository : MongoRepository<Note, String> {
    fun findAllByUserId(userId: String): List<Note>
    fun findAllByUserIdAndUrl(userId: String, url: String): List<Note>
    fun deleteByUserId(userId: String)
}
