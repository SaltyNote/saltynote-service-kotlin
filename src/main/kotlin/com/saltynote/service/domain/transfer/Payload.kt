package com.saltynote.service.domain.transfer

import jakarta.validation.constraints.Email

data class Payload(@Email val email: String)
