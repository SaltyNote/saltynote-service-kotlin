package com.saltynote.service.repository

import com.saltynote.service.entity.LoginHistory
import org.springframework.data.mongodb.repository.MongoRepository

interface LoginHistoryRepository : MongoRepository<LoginHistory, Long> {
    fun deleteByUserId(userId: Long)
}
