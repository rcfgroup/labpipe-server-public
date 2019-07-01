package uk.ac.le.ember.labpipe.server.notification

import org.simplejavamail.email.AttachmentResource
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.email.Recipient
import org.simplejavamail.mailer.Mailer
import org.simplejavamail.mailer.MailerBuilder
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData
import javax.activation.FileDataSource

data class SJMAttachment (var name: String, var file: FileDataSource)

object SJMEmailUtil {
    lateinit var mailer: Mailer

    fun connect(): Mailer {
        mailer = MailerBuilder.withSMTPServerHost(RuntimeData.labPipeConfig.emailHost)
            .withSMTPServerPort(RuntimeData.labPipeConfig.emailPort)
            .withSMTPServerUsername(RuntimeData.labPipeConfig.emailUser)
            .withSMTPServerPassword(RuntimeData.labPipeConfig.emailPass).buildMailer()
        return mailer
    }

    fun sendEmail(
        from: Recipient,
        to: List<Recipient>,
        subject: String,
        text: String,
        html: String,
        attachments: List<SJMAttachment>,
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
        mailer.sendMail(email, async)
    }
}