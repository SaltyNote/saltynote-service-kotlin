package com.saltynote.service.security

import com.saltynote.service.domain.LoginUser
import com.saltynote.service.domain.transfer.TokenPair
import com.saltynote.service.domain.transfer.UserCredential
import com.saltynote.service.service.JwtService
import com.saltynote.service.service.UserService
import com.saltynote.service.service.VaultService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service

@Service
class JWTAuthenticationService(
    val authenticationManager: AuthenticationManager,
    val vaultService: VaultService,
    val jwtService: JwtService,
    val userService: UserService,
) {
    fun authenticate(credential: UserCredential, request: HttpServletRequest): TokenPair {
        val auth: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                credential.username, credential.password, emptyList<GrantedAuthority>()
            )
        )
        val user = auth.principal as LoginUser
        val accessToken: String = jwtService.createAccessToken(user)
        val refreshToken: String = vaultService.fetchOrCreateRefreshToken(user)
        // update current user's lastLoginTime, after user logged in successfully
        userService.saveLoginHistory(
            user.getId()!!, request.getHeader(SecurityConstants.REAL_IP_HEADER),
            request.getHeader(SecurityConstants.USER_AGENT_HEADER)
        )
        return TokenPair(accessToken, refreshToken)
    }
}
