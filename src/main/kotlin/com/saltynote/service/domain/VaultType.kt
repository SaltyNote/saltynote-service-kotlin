package com.saltynote.service.domain

enum class VaultType(val value: String) {
    PASSWORD("password"),
    NEW_ACCOUNT("new_account"),
    REFRESH_TOKEN("refresh_token")
}
