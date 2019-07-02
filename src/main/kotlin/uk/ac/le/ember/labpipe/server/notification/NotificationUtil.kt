package uk.ac.le.ember.labpipe.server.notification

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.data.EmailGroup
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData
import uk.ac.le.ember.labpipe.server.sessions.StaticValue

object NotificationUtil {
    fun sendNotificationEmail(operator: Operator, formCode: String) {
        GlobalScope.launch {
            val recordForm = RuntimeData.mongoDatabase.getCollection<FormTemplate>(StaticValue.DB_MONGO_COL_FORM_TEMPLATES)
                .findOne { FormTemplate::code eq formCode }
            val recipients = getEmailRecipients(operator, recordForm)
            recipients?.run {
                for (r in recipients) {
                    println("Recipient: ${r.name} <${r.address}>")
                }
                EmailUtil.sendEmail(
                    from = Recipient(RuntimeData.labPipeConfig.notificationEmailName, RuntimeData.labPipeConfig.notificationEmailAddress, null),
                    to = recipients,
                    subject = "Subject TO BE REPLACED WITH VARIABLE",
                    text = "TEXT TEMPLATE",
                    html = "HTML TEMPLATE",
                    async = true
                )
            }
        }
    }

    fun getEmailRecipients(operator: Operator, form: FormTemplate?): List<Recipient>? {
        println("Form [${form?.code}] requests notification style: ${form?.notificationStyle}")
        when (form?.notificationStyle) {
            null -> return null
            StaticValue.NOTIFICATION_STYLE_DO_NOT_NOTIFY -> return null
            StaticValue.NOTIFICATION_STYLE_OPERATOR_ONLY -> return mutableListOf(Recipient(
                operator.name,
                operator.email,
                null
            ))
            else -> {
                val emailGroups =
                    RuntimeData.mongoDatabase.getCollection<EmailGroup>(StaticValue.DB_MONGO_COL_FORM_TEMPLATES)
                        .find(EmailGroup::code `in` operator.notificationGroup, EmailGroup::formCode eq form.code)
                for (eg in emailGroups) {
                    println("Found email group: ${eg.code}")
                }
                val adminList =
                    RuntimeData.mongoDatabase.getCollection<Operator>(StaticValue.DB_MONGO_COL_OPERATORS)
                        .find(Operator::username `in` emailGroups.map { g -> g.admin }.flatten()).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                val memberList =
                    RuntimeData.mongoDatabase.getCollection<Operator>(StaticValue.DB_MONGO_COL_OPERATORS)
                        .find(Operator::username `in` emailGroups.map { g -> g.member }.flatten()).toMutableList()
                        .map { o -> Recipient(o.name, o.email, null) }
                return when (form.notificationStyle) {
                    StaticValue.NOTIFICATION_STYLE_ADMIN_ONLY -> adminList
                    StaticValue.NOTIFICATION_STYLE_MEMBER_ONLY -> memberList
                    else -> {
                        (adminList + memberList).distinctBy { it.address }
                    }
                }
            }
        }
    }
}