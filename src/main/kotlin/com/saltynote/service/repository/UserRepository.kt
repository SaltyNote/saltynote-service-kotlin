package com.saltynote.service.repository

import com.saltynote.service.entity.SiteUser
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface UserRepository : MongoRepository<SiteUser, String> {
    fun findByUsername(username: String): SiteUser?
    fun findByEmail(email: String): SiteUser?
}
