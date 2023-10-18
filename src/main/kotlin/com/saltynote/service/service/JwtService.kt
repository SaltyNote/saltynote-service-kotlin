package com.saltynote.service.service

import cn.dev33.satoken.jwt.SaJwtUtil
import cn.dev33.satoken.jwt.StpLogicJwtForStateless
import cn.dev33.satoken.stp.StpUtil
import com.saltynote.service.domain.TokenInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class JwtService(val stpLogic: StpLogicJwtForStateless) {

    @Value("\${jwt.refresh_token.ttl}")
    private var refreshTokenTtl: Long = 0

    fun createRefreshToken(userId: Long): String = StpUtil.getStpLogic()
        .createTokenValue(userId, StpUtil.getLoginDevice(), System.currentTimeMillis() + refreshTokenTtl, emptyMap())


    fun parseToken(token: String): TokenInfo? {
        return try {
            val payloads = SaJwtUtil.parseToken(token, StpUtil.TYPE, stpLogic.jwtSecretKey(), true).payloads
            TokenInfo(
                loginId = payloads.getLong(SaJwtUtil.LOGIN_ID),
                loginType = payloads.getStr(SaJwtUtil.LOGIN_TYPE),
                expireAt = Date(payloads.getLong(SaJwtUtil.EFF)),
                device = payloads.getStr(SaJwtUtil.DEVICE),
            )
        } catch (e: Exception) {
            logger.error(e) { e.message }
            null
        }
    }


}
