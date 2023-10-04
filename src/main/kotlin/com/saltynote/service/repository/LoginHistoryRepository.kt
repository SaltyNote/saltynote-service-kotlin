package com.saltynote.service.repository

import com.saltynote.service.entity.LoginHistory
import org.springframework.data.mongodb.repository.MongoRepository

interface LoginHistoryRepository : MongoRepository<LoginHistory, Int> {
    fun deleteByUserId(userId: String)
}
