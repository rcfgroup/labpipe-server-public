package uk.ac.le.ember.labpipe.server.notification

import org.simplejavamail.email.AttachmentResource
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.email.Recipient
import org.simplejavamail.mailer.Mailer
import org.simplejavamail.mailer.MailerBuilder
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import javax.activation.FileDataSource

data class EmailAttachment(var name: String, var file: FileDataSource)

object EmailUtil {
    fun connect(): Mailer {
        Runtime.mailer = MailerBuilder.withSMTPServerHost(Runtime.config.emailHost)
            .withSMTPServerPort(Runtime.config.emailPort)
            .withSMTPServerUsername(Runtime.config.emailUser)
            .withSMTPServerPassword(Runtime.config.emailPass).buildMailer()
        return Runtime.mailer
    }

    fun testConnection(): Boolean {
        return try {
            Runtime.mailer.testConnection()
            Runtime.logger.info { "Email server connection successful." }
            true
        } catch (e: Exception) {
            if (Runtime.config.debugMode) {
                Runtime.logger.error(e) { "Cannot connect to email server." }
            } else {
                Runtime.logger.error { "Cannot connect to email server." }
            }
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
            Runtime.logger.error(e) { "Error sending email." }
        }
    }
}