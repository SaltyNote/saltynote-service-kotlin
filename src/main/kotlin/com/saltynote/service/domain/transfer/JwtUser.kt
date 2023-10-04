package com.saltynote.service.domain.transfer

import com.saltynote.service.domain.IdentifiableUser


data class JwtUser(private val id: Long, private val username: String) : IdentifiableUser {
    override fun getId() = id

    override fun getUsername() = username
}