package uk.ac.le.ember.labpipe.server.notification

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.data.EmailGroup
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics

object NotificationUtil {
    fun sendNotificationEmail(operator: Operator, formCode: String) {
        GlobalScope.launch {
            val recordForm = Runtime.mongoDatabase.getCollection<FormTemplate>(Statics.DB_MONGO_COL_FORM_TEMPLATES)
                .findOne { FormTemplate::code eq formCode }
            recordForm?.run {
                val recipients = getEmailRecipients(operator, recordForm)
                recipients?.run {
                    for (r in recipients) {
                        println("Recipient: ${r.name} <${r.address}>")
                    }
                    EmailUtil.sendEmail(
                        from = Recipient(
                            Runtime.config.notificationEmailName,
                            Runtime.config.notificationEmailAddress,
                            null
                        ),
                        to = recipients,
                        subject = recordForm.notificationSubject,
                        text = "TEXT TEMPLATE",
                        html = "HTML TEMPLATE",
                        async = true
                    )
                }
            }
        }
    }

    fun getEmailRecipients(operator: Operator, form: FormTemplate?): List<Recipient>? {
        Runtime.logger.info { "Form [${form?.code}] requests notification style: ${form?.notificationStyle}" }
        when (form?.notificationStyle) {
            null -> return null
            Statics.NOTIFICATION_STYLE_DO_NOT_NOTIFY -> return null
            Statics.NOTIFICATION_STYLE_OPERATOR_ONLY -> return mutableListOf(
                Recipient(
                    operator.name,
                    operator.email,
                    null
                )
            )
            else -> {
                val emailGroups =
                    Runtime.mongoDatabase.getCollection<EmailGroup>(Statics.DB_MONGO_COL_EMAIL_GROUPS)
                        .find(EmailGroup::code `in` operator.notificationGroup, EmailGroup::formCode eq form.code)
                val adminUsernames = emailGroups.map { g -> g.admin }.flatten()
                val memberUsernames = emailGroups.map { g -> g.member }.flatten()
                val adminList =
                    Runtime.mongoDatabase.getCollection<Operator>(Statics.DB_MONGO_COL_OPERATORS)
                        .find(Operator::username `in` adminUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                val memberList =
                    Runtime.mongoDatabase.getCollection<Operator>(Statics.DB_MONGO_COL_OPERATORS)
                        .find(Operator::username `in` memberUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                return when (form.notificationStyle) {
                    Statics.NOTIFICATION_STYLE_ADMIN_ONLY -> adminList
                    Statics.NOTIFICATION_STYLE_MEMBER_ONLY -> memberList
                    else -> {
                        (adminList + memberList).distinctBy { it.address }
                    }
                }
            }
        }
    }
}