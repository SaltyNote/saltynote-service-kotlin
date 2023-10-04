package com.saltynote.service.event

import com.saltynote.service.domain.EmailPayload
import com.saltynote.service.service.EmailService
import com.saltynote.service.service.VaultService
import freemarker.template.TemplateException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.mail.MessagingException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.io.IOException

private val logger = KotlinLogging.logger {}

@Component
class EmailEventListener(private val emailService: EmailService, private val vaultService: VaultService) {
    @Value("\${spring.mail.username}")
    private val mailUser: String? = null

    @EventListener
    @Throws(MessagingException::class, IOException::class, TemplateException::class)
    fun handleEvent(event: EmailEvent) {
        logger.info { "Event is received for ${event.type}" }
        val vault = if (event.type === EmailEvent.Type.NEW_USER)
            vaultService.createVerificationCode(event.user.email!!)
        else vaultService.create(event.user.getId()!!, event.type.getVaultType())
        val payload: EmailPayload = event.type
            .loadUser(event.user)
            .loadVault(vault, vaultService.encode(vault))
            .payload
        if (StringUtils.hasText(mailUser)) {
            emailService.sendAsHtml(event.user.email, event.type.subject, payload)
        } else {
            logger.info { "============================================" }
            logger.info { "Email Payload Info = $payload" }
            logger.info { "============================================" }
        }
    }
}
