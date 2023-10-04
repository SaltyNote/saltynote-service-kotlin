package com.saltynote.service.service

import com.saltynote.service.domain.LoginUser
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(val userService: UserService) : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): LoginUser {
        val user = userService.getByUsername(username) ?: throw UsernameNotFoundException(username)
        return LoginUser(user)
    }
}
