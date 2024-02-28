package com.saltyee.golink.interceptor


import com.saltyee.golink.handler.JwtHandler
import com.saltyee.golink.security.UserSignIn
import com.saltynote.service.security.SecurityConstants.AUTH_HEADER
import com.saltynote.service.security.SecurityConstants.CLAIM_KEY_USER_ID
import com.saltynote.service.utils.BaseUtils
import com.saltynote.service.utils.getUserId
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

private val logger = KotlinLogging.logger {}

@Component
class SignInCheckInterceptor(val jwtHandler: JwtHandler) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true

        val signInCheck = handler.method.getAnnotation(UserSignIn::class.java) ?: return true
        if (!signInCheck.required) return true

        try {
            val accessToken = request.getHeader(AUTH_HEADER)
            val decodedJWT = jwtHandler.verifyAccessToken(accessToken)

            RequestContextHolder.currentRequestAttributes().setAttribute(
                CLAIM_KEY_USER_ID,
                decodedJWT.getUserId(),
                RequestAttributes.SCOPE_REQUEST
            )

            return true
        } catch (e: Exception) {
            logger.warn(e) { "SignInCheckInterceptor: ${e.message}" }
            BaseUtils.buildErrorResponse(response, HttpStatus.BAD_REQUEST, "Invalid access token provided!")
            return false
        }
    }
}