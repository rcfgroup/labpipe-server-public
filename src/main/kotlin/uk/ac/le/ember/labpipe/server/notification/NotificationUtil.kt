package uk.ac.le.ember.labpipe.server.notification

import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.EmailGroup
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.Operator
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object NotificationUtil {
    fun sendNotificationEmail(operator: Operator, formIdentifier: String, record: JsonObject) {
        GlobalScope.launch {
            val recordForm =
                Runtime.mongoDatabase.getCollection<FormTemplate>(Constants.MONGO.REQUIRED_COLLECTIONS.FORMS)
                    .findOne { FormTemplate::identifier eq formIdentifier }
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
                        html = htmlReport ?: "Unable to generate html",
                        async = true
                    )
                }
            }
        }
    }

    fun getEmailRecipients(operator: Operator, form: FormTemplate?): List<Recipient>? {
        Runtime.logger.info { "Form [${form?.identifier}] requests notification style: ${form?.notificationStyle}" }
        when (form?.notificationStyle) {
            null -> return null
            Constants.NOTIFICATION.STYLE.DO_NOT_NOTIFY -> return null
            Constants.NOTIFICATION.STYLE.OPERATOR_ONLY -> return mutableListOf(
                Recipient(
                    operator.name,
                    operator.email,
                    null
                )
            )
            else -> {
                val emailGroups =
                    Runtime.mongoDatabase.getCollection<EmailGroup>(Constants.MONGO.REQUIRED_COLLECTIONS.EMAIL_GROUPS)
                        .find(EmailGroup::identifier `in` operator.notificationGroup, EmailGroup::formIdentifier eq form.identifier)
                        .toMutableList()
                println("Email Groups: $emailGroups")
                val adminUsernames = emailGroups.map { g -> g.admin }.flatten()
                println("Admins: $adminUsernames")
                val memberUsernames = emailGroups.map { g -> g.member }.flatten()
                val adminList =
                    Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
                        .find(Operator::username `in` adminUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                val memberList =
                    Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
                        .find(Operator::username `in` memberUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                return when (form.notificationStyle) {
                    Constants.NOTIFICATION.STYLE.ADMIN_ONLY -> adminList
                    Constants.NOTIFICATION.STYLE.MEMBER_ONLY -> memberList
                    Constants.NOTIFICATION.STYLE.NOTIFY_ALL -> {
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