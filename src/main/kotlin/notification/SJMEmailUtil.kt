package notification

import configs.LabPipeConfig
import org.simplejavamail.email.AttachmentResource
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.email.Recipient
import org.simplejavamail.mailer.Mailer
import org.simplejavamail.mailer.MailerBuilder
import javax.activation.FileDataSource

data class SJMAttachment (var name: String, var file: FileDataSource)

object SJMEmailUtil {
    lateinit var mailer: Mailer

    fun connect(config: LabPipeConfig): Mailer {
        mailer = MailerBuilder.withSMTPServerHost(config.emailHost)
            .withSMTPServerPort(config.emailPort)
            .withSMTPServerUsername(config.emailUser)
            .withSMTPServerPassword(config.emailPass).buildMailer()
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