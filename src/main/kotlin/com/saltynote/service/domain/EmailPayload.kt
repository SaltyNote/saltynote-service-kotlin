package com.saltynote.service.domain


data class EmailPayload(
    var username: String? = null,
    val message: String,
    var link: String? = null,
    var linkText: String? = null,
)
