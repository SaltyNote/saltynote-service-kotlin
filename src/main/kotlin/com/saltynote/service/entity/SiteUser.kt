package com.saltynote.service.entity

import com.saltynote.service.domain.IdentifiableUser
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serial
import java.io.Serializable

@Document
data class SiteUser(
    @Id
    val id: String? = null,
    val username: @NotBlank String? = null,
    val email: @NotBlank @Email String? = null,
    var password: @NotBlank String? = null,
    val registerTime: Long = System.currentTimeMillis(),
) : Serializable, IdentifiableUser {


    companion object {
        @Serial
        private val serialVersionUID = 1L
    }

    override fun getUsername() = username

    override fun getId() = id
}
