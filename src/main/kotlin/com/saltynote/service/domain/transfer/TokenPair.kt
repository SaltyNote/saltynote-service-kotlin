package com.saltynote.service.domain.transfer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TokenPair(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: @NotBlank String?
)
