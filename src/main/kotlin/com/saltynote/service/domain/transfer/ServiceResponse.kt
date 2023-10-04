package com.saltynote.service.domain.transfer

import org.springframework.http.HttpStatus

data class ServiceResponse(val status: HttpStatus, val message: String = "") {
    companion object {
        fun ok(welcomeMessage: String): ServiceResponse {
            return ServiceResponse(HttpStatus.OK, welcomeMessage)
        }
    }
}