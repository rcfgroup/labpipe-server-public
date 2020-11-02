package uk.ac.le.ember.labpipe.server.controllers

import mu.KotlinLogging
import org.simplejavamail.api.email.AttachmentResource
import org.simplejavamail.api.email.Recipient
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import uk.ac.le.ember.labpipe.server.controllers.ConfigController.Companion.LabPipeConfig.Email
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import javax.activation.FileDataSource

data class EmailAttachment(var name: String, var file: FileDataSource)

private val logger = KotlinLogging.logger {}

class EmailController {

    companion object {
        fun connect(): Mailer {
            Runtime.mailer = MailerBuilder.withSMTPServerHost(Runtime.config[Email.host])
                .withSMTPServerPort(Runtime.config[Email.port])
                .withSMTPServerUsername(Runtime.config[Email.user])
                .withSMTPServerPassword(Runtime.config[Email.password]).buildMailer()
            return Runtime.mailer
        }

        fun testConnection(): Boolean {
            connect()
            return try {
                Runtime.mailer.testConnection()
                logger.info { "Email server connection successful." }
                true
            } catch (e: Exception) {
                logger.error(e) { "Cannot connect to email server." }
                false
            }
        }

        fun sendEmail(
            from: Recipient,
            to: List<Recipient>,
            subject: String,
            text: String,
            html: String,
            attachments: List<EmailAttachment> = mutableListOf(),
            async: Boolean
        ) {
            try {
                Runtime.mailer.testConnection()
                val email = EmailBuilder.startingBlank()
                    .from(from)
                    .to(to)
                    .withSubject(subject)
                    .withPlainText(text)
                    .withHTMLText(html)
                    .withAttachments(attachments.map { x -> AttachmentResource(x.name, x.file) })
                    .buildEmail()
                Runtime.mailer.sendMail(email, async)
            } catch (e: Exception) {
                logger.error(e) { "Error sending email." }
            }
        }
    }

}