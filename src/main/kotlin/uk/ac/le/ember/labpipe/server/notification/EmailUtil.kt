package uk.ac.le.ember.labpipe.server.notification

import org.simplejavamail.email.AttachmentResource
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.email.Recipient
import org.simplejavamail.mailer.Mailer
import org.simplejavamail.mailer.MailerBuilder
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData
import javax.activation.FileDataSource

data class EmailAttachment (var name: String, var file: FileDataSource)

object EmailUtil {
    fun connect(): Mailer {
        RuntimeData.mailer = MailerBuilder.withSMTPServerHost(RuntimeData.labPipeConfig.emailHost)
            .withSMTPServerPort(RuntimeData.labPipeConfig.emailPort)
            .withSMTPServerUsername(RuntimeData.labPipeConfig.emailUser)
            .withSMTPServerPassword(RuntimeData.labPipeConfig.emailPass).buildMailer()
        return RuntimeData.mailer
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
        val email = EmailBuilder.startingBlank()
            .from(from)
            .to(to)
            .withSubject(subject)
            .withPlainText(text)
            .withHTMLText(html)
            .withAttachments(attachments.map { x -> AttachmentResource(x.name, x.file) })
            .buildEmail()
        RuntimeData.mailer.sendMail(email, async)
    }
}