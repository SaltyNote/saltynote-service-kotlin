package com.saltynote.service.security

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.saltynote.service.domain.transfer.JwtUser
import com.saltynote.service.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.lang.NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JWTAuthorizationFilter(val jwtService: JwtService) : OncePerRequestFilter() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        @NonNull req: HttpServletRequest, @NonNull res: HttpServletResponse,
        @NonNull chain: FilterChain
    ) {
        val header = req.getHeader(SecurityConstants.AUTH_HEADER)
        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res)
            return
        }
        val authentication = getAuthentication(req)
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(req, res)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(SecurityConstants.AUTH_HEADER) ?: return null
        var decodedJWT: DecodedJWT? = null
        try {
            // parse the token.
            decodedJWT = jwtService.verifyAccessToken(token)
            if (decodedJWT == null) {
                return null
            }
        } catch (e: JWTVerificationException) {
            return null
        }
        return UsernamePasswordAuthenticationToken(
            JwtUser(
                decodedJWT.getClaim(SecurityConstants.CLAIM_KEY_USER_ID).asString(),
                decodedJWT.subject
            ),
            null, emptyList()
        )
    }
}
