package com.saltynote.service.domain.transfer

import jakarta.validation.constraints.NotBlank

class PasswordUpdate(
    val oldPassword: @NotBlank String,
    val password: @NotBlank String,
)