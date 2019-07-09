package uk.ac.le.ember.labpipe.server.notification

import com.google.gson.JsonObject
import j2html.TagCreator.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.data.EmailGroup
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.data.ReportTemplate
import uk.ac.le.ember.labpipe.server.sessions.NotificationStyle
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics

object NotificationUtil {
    fun sendNotificationEmail(operator: Operator, formCode: String, record: JsonObject) {
        GlobalScope.launch {
            val recordForm = Runtime.mongoDatabase.getCollection<FormTemplate>(RequiredMongoDBCollections.FORM_TEMPLATES.value)
                .findOne { FormTemplate::code eq formCode }
            recordForm?.run {
                val recipients = getEmailRecipients(operator, recordForm)
                recipients?.run {
                    for (r in recipients) {
                        println("Recipient: ${r.name} <${r.address}>")
                    }
                    var htmlReport = generateReportHtml(operator, recordForm, record)
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
                val adminUsernames = emailGroups.map { g -> g.admin }.flatten()
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

    fun generateReportHtml(operator: Operator, form: FormTemplate, formData: JsonObject) {
        val reportTemplate = Runtime.mongoDatabase.getCollection<ReportTemplate>(RequiredMongoDBCollections.REPORT_TEMPLATES.value)
            .findOne (ReportTemplate::formCode eq form.code, ReportTemplate::active eq true )
        reportTemplate?.run {
            val elements = reportTemplate.htmlTemplate
            elements.sortBy { it.order }
            var report = body()
            elements.forEach {
                run {
                    when (it.type) {
                        "report_title" -> {
                            val valueArray = it.value.split("::")
                            val value: String = when (valueArray[0]) {
                                "static" -> valueArray[1]
                                "form_data" -> getFormDataAsString(formData, valueArray[1])
                                else -> "Invalid format in report config"
                            }
                            report.with(h1(value))
                        }
                        "section_title" -> {
                            val valueArray = it.value.split("::")
                            val value: String = when (valueArray[0]) {
                                "static" -> valueArray[1]
                                "form_data" -> getFormDataAsString(formData, valueArray[1])
                                else -> "Invalid format in report config"
                            }
                            report.with(h3(value))
                        }
                        "table_keyvalue" -> {

                        }
                        else -> report.with(p("Invalid format in report config"))
                    }
                }
            }
        }
    }

    fun getFormDataAsString(formData: JsonObject, keyString: String): String {
        if (keyString.contains('.')) {
            val keyArray = keyString.split(".")
            return formData.getAsJsonObject(keyArray[0]).get(keyArray[1]).asString
        } else {
            return formData.get(keyString).asString
        }
    }
}