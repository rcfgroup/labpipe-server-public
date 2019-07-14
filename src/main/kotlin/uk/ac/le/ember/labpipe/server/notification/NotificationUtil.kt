package uk.ac.le.ember.labpipe.server.notification

import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.data.EmailGroup
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.sessions.NotificationStyle
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object NotificationUtil {
    fun sendNotificationEmail(operator: Operator, formCode: String, record: JsonObject) {
        GlobalScope.launch {
            val recordForm =
                Runtime.mongoDatabase.getCollection<FormTemplate>(RequiredMongoDBCollections.FORM_TEMPLATES.value)
                    .findOne { FormTemplate::code eq formCode }
            recordForm?.run {
                val recipients = getEmailRecipients(operator, recordForm)
                recipients?.run {
                    for (r in recipients) {
                        println("Recipient: ${r.name} <${r.address}>")
                    }
                    var htmlReport = ReportUtil.generateHtml(operator, recordForm, record)
                    EmailUtil.sendEmail(
                        from = Recipient(
                            Runtime.config.notificationEmailName,
                            Runtime.config.notificationEmailAddress,
                            null
                        ),
                        to = recipients,
                        subject = recordForm.notificationSubject,
                        text = "TEXT TEMPLATE",
                        html = htmlReport ?: "Unable to generate html code",
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
            NotificationStyle.DO_NOT_NOTIFY.value -> return null
            NotificationStyle.OPERATOR_ONLY.value -> return mutableListOf(
                Recipient(
                    operator.name,
                    operator.email,
                    null
                )
            )
            else -> {
                val emailGroups =
                    Runtime.mongoDatabase.getCollection<EmailGroup>(RequiredMongoDBCollections.EMAIL_GROUPS.value)
                        .find(EmailGroup::code `in` operator.notificationGroup, EmailGroup::formCode eq form.code)
                        .toMutableList()
                println("Email Groups: $emailGroups")
                val adminUsernames = emailGroups.map { g -> g.admin }.flatten()
                println("Admins: $adminUsernames")
                val memberUsernames = emailGroups.map { g -> g.member }.flatten()
                val adminList =
                    Runtime.mongoDatabase.getCollection<Operator>(RequiredMongoDBCollections.OPERATORS.value)
                        .find(Operator::username `in` adminUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                val memberList =
                    Runtime.mongoDatabase.getCollection<Operator>(RequiredMongoDBCollections.OPERATORS.value)
                        .find(Operator::username `in` memberUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                return when (form.notificationStyle) {
                    NotificationStyle.ADMIN_ONLY.value -> adminList
                    NotificationStyle.MEMBER_ONLY.value -> memberList
                    NotificationStyle.NOTIFY_ALL.value -> {
                        (adminList + memberList).distinctBy { it.address }
                    }
                    else -> {
                        (adminList + memberList).distinctBy { it.address }
                    }
                }
            }
        }
    }
}