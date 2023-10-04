package com.saltynote.service.controller.advice

import com.saltynote.service.domain.transfer.ServiceResponse
import com.saltynote.service.exception.WebAppRuntimeException
import io.sentry.Sentry
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandlerControllerAdvice {
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<ServiceResponse> {
        Sentry.captureException(e)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ServiceResponse(HttpStatus.UNAUTHORIZED, e.message))
    }

    @ExceptionHandler(WebAppRuntimeException::class)
    fun handleWebClientRuntimeException(e: WebAppRuntimeException): ResponseEntity<ServiceResponse> {
        Sentry.captureException(e)
        return ResponseEntity.status(e.status).body(ServiceResponse(e.status, e.message))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ServiceResponse> {
        Sentry.captureException(e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ServiceResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Something is going wrong with the server, please try again later."
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleRuntimeException(e: Exception): ResponseEntity<ServiceResponse> {
        Sentry.captureException(e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ServiceResponse(
                    HttpStatus.BAD_REQUEST,
                    "Something is going wrong with your request, please try again later."
                )
            )
    }
}
