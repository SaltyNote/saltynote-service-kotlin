package com.saltynote.service.service

import com.saltynote.service.domain.EmailPayload
import freemarker.template.Configuration
import freemarker.template.TemplateException
import jakarta.mail.MessagingException
import jakarta.validation.constraints.NotNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import java.io.IOException
import java.util.*

@Service
class EmailService(private val mailSender: JavaMailSender) {
    @Value("\${spring.mail.username}")
    private val emailSender: String? = null
    private val freemarkerConfig: Configuration = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)

    init {
        freemarkerConfig.setClassForTemplateLoading(this.javaClass, "/templates/")
    }

    fun send(receiver: String?, subject: String?, message: String?) {
        val mailMessage = SimpleMailMessage()
        mailMessage.setTo(receiver)
        mailMessage.subject = subject
        mailMessage.text = message
        mailMessage.from = emailSender
        mailSender.send(mailMessage)
    }

    @Throws(MessagingException::class, IOException::class, TemplateException::class)
    fun sendAsHtml(receiver: String?, subject: String?, emailPayload: @NotNull EmailPayload?) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setTo(receiver!!)
        helper.setSubject(subject!!)
        helper.setFrom(emailSender!!)
        // Use the true flag to indicate the text included is HTML
        val t = freemarkerConfig.getTemplate("email/general.ftlh")
        val content = FreeMarkerTemplateUtils.processTemplateIntoString(
            t,
            Collections.singletonMap("payload", emailPayload)
        )
        helper.setText(content, true)
        mailSender.send(message)
    }
}
