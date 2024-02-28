package com.saltyee.golink.handler

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.saltyee.golink.configration.JwtProperties
import com.saltyee.golink.security.SecurityConstants
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class JwtHandler(val jwtProperties: JwtProperties) {

    private lateinit var accessTokenVerifier: JWTVerifier

    private lateinit var refreshTokenVerifier: JWTVerifier

    @PostConstruct
    fun init() {
        accessTokenVerifier = JWT.require(Algorithm.HMAC512(jwtProperties.accessToken.secret.toByteArray())).build()
        refreshTokenVerifier = JWT.require(Algorithm.HMAC512(jwtProperties.refreshToken.secret.toByteArray())).build()
    }


    fun createAccessToken(userId: Long): String = createToken(userId, jwtProperties.accessToken)
    fun createRefreshToken(userId: Long): String = createToken(userId, jwtProperties.refreshToken)


    fun verifyAccessToken(token: String): DecodedJWT {
        // This will also handle token expiration exception
        return accessTokenVerifier.verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
    }


    fun verifyRefreshToken(token: String?): DecodedJWT {
        // This will also handle token expiration exception
        return refreshTokenVerifier.verify(token)
    }


    fun createToken(
        userId: Long,
        tokenConf: JwtProperties.TokenConf
    ): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + tokenConf.ttl * 1000))
            .sign(Algorithm.HMAC512(tokenConf.secret.toByteArray()))
    }

    fun isRefreshTokenReusable(refreshToken: String): Boolean {
        return try {
            val decodedJWT = verifyRefreshToken(refreshToken)
            // at least 60% of the token's lifetime
            return decodedJWT.expiresAt
                .after(Date(System.currentTimeMillis() + jwtProperties.refreshToken.ttl * 600))
        } catch (e: Exception) {
            false
        }
    }
}
