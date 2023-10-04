package com.saltynote.service.entity

import com.saltynote.service.domain.Identifiable
import jakarta.validation.constraints.Email
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serial
import java.io.Serializable

@Document
data class Vault(
    @Id
    val id: String? = null,
    val userId: String? = null,
    val secret: String? = null,
    val type: String? = null,
    @Email
    val email: String? = null,
    val createdTime: Long = System.currentTimeMillis()
) : Serializable, Identifiable {


    companion object {
        @Serial
        private val serialVersionUID = 1L
    }

    override fun getId() = id
}
