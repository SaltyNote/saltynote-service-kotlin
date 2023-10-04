package com.saltynote.service.domain.transfer

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.saltynote.service.entity.User
import jakarta.validation.constraints.NotBlank

@JsonIgnoreProperties(ignoreUnknown = true)
open class UserCredential(
    @NotBlank open val username: String,
    @NotBlank open val password: String,
    @NotBlank open val email: String,
) {

    @JsonIgnore
    fun toSiteUser(): User {
        return User(email = email, username = username, password = password)
    }
}
