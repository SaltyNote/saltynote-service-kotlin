package com.saltynote.service.event

import com.saltynote.service.domain.EmailPayload
import com.saltynote.service.domain.VaultType
import com.saltynote.service.entity.User
import com.saltynote.service.entity.Vault
import com.saltynote.service.utils.BaseUtils
import org.springframework.context.ApplicationEvent

class EmailEvent(source: Any, val user: User, val type: Type) : ApplicationEvent(source) {

    enum class Type(val subject: String, val payload: EmailPayload) {
        NEW_USER(
            "Signup Code to SaltyNote!",
            EmailPayload(message = "Below is the code you will use for signup.")
        ) {
            override fun loadVault(vault: Vault, encodedVault: String?): Type {
                this.payload.linkText = vault.secret
                return this
            }

            override fun getVaultType(): VaultType {
                return VaultType.NEW_ACCOUNT
            }
        },
        PASSWORD_FORGET(
            "Password Reset from SaltyNote!",
            EmailPayload(linkText = "Reset Your Password", message = "Below is the link for you to reset your password.")
        ) {
            override fun loadVault(vault: Vault, encodedVault: String?): Type {
                this.payload.link = BaseUtils.getPasswordResetUrl(encodedVault)
                return this
            }

            override fun getVaultType(): VaultType {
                return VaultType.PASSWORD
            }
        };

        fun loadUser(user: User): Type {
            payload.username = user.username
            return this
        }

        abstract fun loadVault(vault: Vault, encodedVault: String?): Type
        abstract fun getVaultType(): VaultType
    }
}
