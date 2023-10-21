package com.saltynote.service.controller.advice

import cn.dev33.satoken.exception.NotLoginException
import com.saltynote.service.domain.transfer.ServiceResponse
import com.saltynote.service.exception.WebAppRuntimeException
import io.sentry.Sentry
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandlerControllerAdvice {

    @ExceptionHandler(WebAppRuntimeException::class)
    fun handleWebClientRuntimeException(e: WebAppRuntimeException): ResponseEntity<ServiceResponse> {
        Sentry.captureException(e)
        return ResponseEntity.status(e.status).body(ServiceResponse(e.status, e.message))
    }

    @ExceptionHandler(NotLoginException::class)
    fun handleWebClientRuntimeException(e: NotLoginException): ResponseEntity<ServiceResponse> {
        Sentry.captureException(e)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ServiceResponse(HttpStatus.FORBIDDEN, e.localizedMessage ?: "Not Login"))
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
