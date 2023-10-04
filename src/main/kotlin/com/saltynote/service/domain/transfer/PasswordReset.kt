package com.saltynote.service.domain.transfer

import jakarta.validation.constraints.NotBlank

class PasswordReset(
    val token: @NotBlank String,
    val password: @NotBlank String,
)