package com.saltynote.service.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.saltynote.service.domain.IdentifiableUser
import com.saltynote.service.domain.transfer.JwtUser
import com.saltynote.service.domain.transfer.TokenPair
import com.saltynote.service.security.SecurityConstants
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtService(private val objectMapper: ObjectMapper) {
    @Value("\${jwt.access_token.secret}")
    private var accessTokenSecret: String? = null

    @Value("\${jwt.access_token.ttl}")
    private var accessTokenTtl: Long = 0

    @Value("\${jwt.refresh_token.secret}")
    private var refreshTokenSecret: String? = null

    @Value("\${jwt.refresh_token.ttl}")
    private var refreshTokenTtl: Long = 0


    private var accessTokenVerifier: JWTVerifier? = null
    private var refreshTokenVerifier: JWTVerifier? = null

    @PostConstruct
    fun init() {
        accessTokenVerifier = JWT.require(Algorithm.HMAC512(accessTokenSecret!!.toByteArray())).build()
        refreshTokenVerifier = JWT.require(Algorithm.HMAC512(refreshTokenSecret!!.toByteArray())).build()
    }

    fun createAccessToken(user: IdentifiableUser): String {
        return createToken(user, accessTokenTtl, accessTokenSecret)
    }

    fun createRefreshToken(user: IdentifiableUser): String {
        return createToken(user, refreshTokenTtl, refreshTokenSecret)
    }

    private fun createToken(user: IdentifiableUser, tokenTTL: Long, secret: String?): String {
        return createToken(user.getUsername()!!, user.getId()!!, tokenTTL, secret)
    }

    @Throws(JWTCreationException::class)
    private fun createToken(
        subject: String, userId: String, tokenTTL: Long,
        secret: String?
    ): String {
        return JWT.create()
            .withSubject(subject)
            .withClaim(SecurityConstants.CLAIM_KEY_USER_ID, userId)
            .withExpiresAt(Date(System.currentTimeMillis() + tokenTTL))
            .sign(Algorithm.HMAC512(secret!!.toByteArray()))
    }

    @Throws(JWTVerificationException::class)
    fun verifyAccessToken(token: String): DecodedJWT {
        // This will also handle token expiration exception
        return accessTokenVerifier!!.verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
    }

    @Throws(JWTVerificationException::class)
    fun verifyRefreshToken(token: String?): DecodedJWT {
        // This will also handle token expiration exception
        return refreshTokenVerifier!!.verify(token)
    }

    @Throws(JWTVerificationException::class)
    fun parseRefreshToken(token: String?): JwtUser {
        val jwt = verifyRefreshToken(token)
        return JwtUser(jwt.getClaim(SecurityConstants.CLAIM_KEY_USER_ID).asString(), jwt.subject)
    }

    @Throws(JsonProcessingException::class)
    fun tokenToJson(accessToken: String, refreshToken: String?): String {
        return objectMapper.writeValueAsString(TokenPair(accessToken, refreshToken))
    }
}
