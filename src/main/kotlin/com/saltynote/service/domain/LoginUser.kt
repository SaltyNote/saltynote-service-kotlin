package com.saltynote.service.domain

import com.saltynote.service.entity.User
import org.springframework.security.core.GrantedAuthority

data class LoginUser(val user: User) : User(user.username, user.password, emptyList<GrantedAuthority>()), IdentifiableUser {
    override fun getId() = user.id
}
