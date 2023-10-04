package com.saltynote.service.domain.transfer

import jakarta.validation.constraints.NotBlank

class UserNewRequest(
    @NotBlank val token: String,
    @NotBlank override val username: String,
    @NotBlank override val password: String,
    @NotBlank override val email: String,
) : UserCredential(username, password, email)