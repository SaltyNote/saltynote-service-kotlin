package com.saltynote.service.domain

import com.saltynote.service.entity.SiteUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

data class LoginUser(val user: SiteUser) : User(user.username, user.password, emptyList<GrantedAuthority>()), IdentifiableUser {
    override fun getId() = user.id
}
