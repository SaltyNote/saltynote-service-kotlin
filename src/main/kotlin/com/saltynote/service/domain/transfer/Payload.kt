package com.saltynote.service.domain.transfer

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class Payload(@NotBlank @Email val email: String)
