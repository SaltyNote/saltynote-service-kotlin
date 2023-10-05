package com.saltynote.service.exception

import org.springframework.http.HttpStatus

data class WebAppRuntimeException(val status: HttpStatus, override val message: String) : RuntimeException(message)
