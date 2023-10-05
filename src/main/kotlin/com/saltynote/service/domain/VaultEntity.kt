package com.saltynote.service.domain

import com.saltynote.service.entity.Vault

data class VaultEntity(val userId: Long, val secret: String) {


    companion object {
        fun from(vault: Vault): VaultEntity {
            return VaultEntity(secret = vault.secret, userId = vault.userId)
        }
    }
}
