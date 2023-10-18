package com.saltynote.service.controller

import cn.dev33.satoken.secure.BCrypt
import cn.dev33.satoken.stp.StpUtil
import com.saltynote.service.domain.VaultType
import com.saltynote.service.domain.transfer.*
import com.saltynote.service.entity.User
import com.saltynote.service.event.EmailEvent
import com.saltynote.service.exception.WebAppRuntimeException
import com.saltynote.service.security.SecurityConstants
import com.saltynote.service.service.JwtService
import com.saltynote.service.service.UserService
import com.saltynote.service.service.VaultService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*

private val logger = KotlinLogging.logger {}

@Tag(name = "UserController", description = "User related APIs")
@RestController
class UserController(
    val userService: UserService,
    val jwtService: JwtService,
    val eventPublisher: ApplicationEventPublisher,
    val vaultService: VaultService,
) {
    @Value("\${password.minimal.length}")
    private val passwordMinimalLength = 0

    @PostMapping("/email/verification")
    fun getVerificationToken(@RequestBody @Valid payload: Payload): ResponseEntity<ServiceResponse> {
        // check whether this email is already signed up or not.
        if (userService.getByEmail(payload.email) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ServiceResponse(HttpStatus.BAD_REQUEST, "Email is already signed up."))
        }
        val user = User(email = payload.email, username = "there")
        eventPublisher.publishEvent(EmailEvent(this, user, EmailEvent.Type.NEW_USER))
        return ResponseEntity.ok(ServiceResponse.ok("A verification code for signup is sent to you email now"))
    }

    @Operation(summary = "User Signup", description = "Verification token is needed for signup.")
    @PostMapping("/signup")
    fun signup(@RequestBody userNewRequest: @Valid UserNewRequest): ResponseEntity<UserDto> {
        if (userNewRequest.password.length < passwordMinimalLength) {
            throw WebAppRuntimeException(
                HttpStatus.BAD_REQUEST,
                "Password should be at least $passwordMinimalLength characters."
            )
        }
        // Check token
        val vaultOp = vaultService.getByEmailAndSecretAndType(
            userNewRequest.email,
            userNewRequest.token, VaultType.NEW_ACCOUNT
        ) ?: throw WebAppRuntimeException(HttpStatus.FORBIDDEN, "A valid verification code is required for signup.")
        var user = userNewRequest.toUser()
        user = userService.create(user)

        vaultService.deleteById(vaultOp.getId())
        return ResponseEntity.ok(UserDto(user.getId(), user.getUsername()))
    }

    @PostMapping("/login")
    fun authenticate(@RequestBody request: UserCredential, req: HttpServletRequest): ResponseEntity<TokenPair> {
        val user = userService.getByEmail(request.email)
        user?.let {
            if (BCrypt.checkpw(request.password, user.password)) {
                StpUtil.login(user.getId())
                val refreshToken = vaultService.fetchOrCreateRefreshToken(user)
                // update current user's lastLoginTime, after user logged in successfully
                userService.saveLoginHistory(
                    user.getId(), req.getHeader(SecurityConstants.REAL_IP_HEADER),
                    req.getHeader(SecurityConstants.USER_AGENT_HEADER)
                )
                return ResponseEntity.ok(TokenPair(accessToken = StpUtil.getTokenValue(), refreshToken = refreshToken))
            }
        }

        return ResponseEntity.notFound().build()
    }

    @PostMapping("/refresh_token")
    fun refreshToken(@RequestBody tokenPair: @Valid TokenPair): ResponseEntity<TokenPair> {
        // 1. No expiry, and valid.
        val token = jwtService.parseToken(tokenPair.refreshToken)
        // 2. Not deleted from database.
        token?.let {
            val vault = vaultService.findByUserIdAndTypeAndValue(
                it.loginId, VaultType.REFRESH_TOKEN,
                tokenPair.refreshToken
            )
            vault?.let { _ ->
                val newToken = jwtService.createRefreshToken(it.loginId)
                return ResponseEntity.ok(TokenPair(newToken, tokenPair.refreshToken))
            }
        }

        throw WebAppRuntimeException(HttpStatus.BAD_REQUEST, "Invalid refresh token provided!")

    }

    @Transactional
    @DeleteMapping("/refresh_tokens")
    fun cleanRefreshTokens(): ResponseEntity<ServiceResponse> {
        val userId = StpUtil.getLoginIdAsLong()
        logger.info { "[cleanRefreshTokens] user = $userId" }
        vaultService.cleanRefreshTokenByUserId(userId)
        return ResponseEntity.ok(ServiceResponse.ok("All your refresh tokens are cleaned."))
    }

    @PostMapping("/password/forget")
    fun forgetPassword(@RequestBody payload: @Valid Payload): ResponseEntity<ServiceResponse> {
        val usero = userService.getByEmail(payload.email)
        if (usero == null) {
            logger.warn { "User is not found for email = ${payload.email}" }
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                .body(ServiceResponse(HttpStatus.PRECONDITION_FAILED, "Invalid email"))
        }
        eventPublisher.publishEvent(EmailEvent(this, usero, EmailEvent.Type.PASSWORD_FORGET))
        return ResponseEntity.ok(
            ServiceResponse
                .ok("Password reset email will be sent to your email, please reset your email with link there.")
        )
    }

    @PostMapping("/password/reset")
    fun resetPassword(@RequestBody passwordReset: @Valid PasswordReset): ResponseEntity<ServiceResponse> {
        val wre = WebAppRuntimeException(HttpStatus.BAD_REQUEST, "Invalid payload provided.")
        if (passwordReset.password.length < passwordMinimalLength) {
            throw WebAppRuntimeException(
                HttpStatus.BAD_REQUEST,
                "Password should be at least $passwordMinimalLength characters."
            )
        }
        val vo = vaultService.findByToken(passwordReset.token) ?: throw wre
        val usero = userService.getById(vo.userId)
        return if (usero.isPresent) {
            val user = usero.get()
            user.password = BCrypt.hashpw(passwordReset.password)
            userService.update(user)
            vaultService.deleteById(vo.getId())
            ResponseEntity.ok(ServiceResponse.ok("Password has been reset!"))
        } else {
            throw wre
        }
    }

    @RequestMapping(value = ["/password"], method = [RequestMethod.POST, RequestMethod.PUT])
    fun updatePassword(
        @RequestBody passwordUpdate: @Valid PasswordUpdate
    ): ResponseEntity<ServiceResponse> {
        val userId = StpUtil.getLoginIdAsLong()
        // Validate new password
        if (passwordUpdate.password.length < passwordMinimalLength) {
            throw WebAppRuntimeException(
                HttpStatus.BAD_REQUEST,
                "New password should be at least $passwordMinimalLength characters."
            )
        }

        // Validate old password
        val usero: Optional<User> = userService.getById(userId)
        if (usero.isEmpty) {
            throw WebAppRuntimeException(
                HttpStatus.BAD_REQUEST,
                "Something goes wrong when fetching your info, please try later again."
            )
        }
        val user: User = usero.get()
        if (!BCrypt.checkpw(passwordUpdate.oldPassword, user.password)) {
            throw WebAppRuntimeException(HttpStatus.BAD_REQUEST, "Wrong current password is provided.")
        }
        user.password = BCrypt.hashpw(passwordUpdate.password)
        userService.update(user)
        return ResponseEntity.ok(ServiceResponse.ok("Password is updated now."))
    }

    @DeleteMapping("/account/{id}")
    fun accountDeletion(@PathVariable("id") userId: Long): ResponseEntity<ServiceResponse> {
        val loginId = StpUtil.getLoginIdAsLong()
        if (userId != loginId) {
            throw WebAppRuntimeException(HttpStatus.BAD_REQUEST, "User information is not confirmed")
        }
        userService.cleanupByUserId(loginId)
        return ResponseEntity.ok(ServiceResponse.ok("Account deletion is successful."))
    }
}
