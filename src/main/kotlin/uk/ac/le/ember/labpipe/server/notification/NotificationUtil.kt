package uk.ac.le.ember.labpipe.server.notification

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import org.simplejavamail.api.email.Recipient
import uk.ac.le.ember.labpipe.server.*
import uk.ac.le.ember.labpipe.server.sessions.Runtime


object NotificationUtil {
    fun sendNotificationEmail(operator: Operator, formIdentifier: String, record: JsonObject) {
        GlobalScope.launch {
            val recordForm = MONGO.COLLECTIONS.FORMS.findOne { FormTemplate::identifier eq formIdentifier }
            recordForm?.run {
                val recipients = getEmailRecipients(operator, recordForm)
                recipients?.run {
                    for (r in recipients) {
                        println("Recipient: ${r.name} <${r.address}>")
                    }
                    var htmlReport = ReportUtil.generateHtml(operator, recordForm, record)
                    EmailUtil.sendEmail(
                        from = Recipient(
                            Runtime.lpConfig.notificationEmailName,
                            Runtime.lpConfig.notificationEmailAddress,
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

    fun sendNotificationEmail(operator: Operator, record: Record) {
        GlobalScope.launch {
            val recordForm = MONGO.COLLECTIONS.FORMS.findOne { FormTemplate::identifier eq record.formIdentifier }
            recordForm?.run {
                val recipients = getEmailRecipients(operator, recordForm)
                recipients?.run {
                    for (r in recipients) {
                        println("Recipient: ${r.name} <${r.address}>")
                    }
                    val recordObject = JsonParser.parseString(Gson().toJson(record)).asJsonObject
                    var htmlReport = ReportUtil.generateHtml(operator, recordForm, recordObject)
                    EmailUtil.sendEmail(
                        from = Recipient(
                            Runtime.lpConfig.notificationEmailName,
                            Runtime.lpConfig.notificationEmailAddress,
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
            NOTIFICATION.STYLE.DO_NOT_NOTIFY -> return null
            NOTIFICATION.STYLE.OPERATOR_ONLY -> return mutableListOf(
                Recipient(
                    operator.name,
                    operator.email,
                    null
                )
            )
            else -> {
                val emailGroups =
                    Runtime.mongoDatabase.getCollection<EmailGroup>(MONGO.COL_NAMES.EMAIL_GROUPS)
                        .find(EmailGroup::identifier `in` operator.notificationGroup, EmailGroup::formIdentifier eq form.identifier)
                        .toMutableList()
                println("Email Groups: $emailGroups")
                val adminUsernames = emailGroups.map { g -> g.admin }.flatten()
                println("Admins: $adminUsernames")
                val memberUsernames = emailGroups.map { g -> g.member }.flatten()
                val adminList =
                    Runtime.mongoDatabase.getCollection<Operator>(MONGO.COL_NAMES.OPERATORS)
                        .find(Operator::username `in` adminUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                val memberList =
                    Runtime.mongoDatabase.getCollection<Operator>(MONGO.COL_NAMES.OPERATORS)
                        .find(Operator::username `in` memberUsernames).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                return when (form.notificationStyle) {
                    NOTIFICATION.STYLE.ADMIN_ONLY -> adminList
                    NOTIFICATION.STYLE.MEMBER_ONLY -> memberList
                    NOTIFICATION.STYLE.NOTIFY_ALL -> {
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