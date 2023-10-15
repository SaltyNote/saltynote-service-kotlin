package com.saltynote.service.domain.transfer

class UserNewRequest(
    val token: String,
    override val username: String,
    override val password: String,
    override val email: String,
) : UserCredential(username, password, email)