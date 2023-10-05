package com.saltynote.service.service

import com.saltynote.service.domain.Identifiable
import java.util.*

interface RepositoryService<K, T : Identifiable> {
    fun create(entity: T): T
    fun update(entity: T): T
    fun getById(id: K): Optional<T>
    fun delete(entity: T)
}
