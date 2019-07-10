package uk.ac.le.ember.labpipe.server.notification

import com.google.gson.JsonArray
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
import uk.ac.le.ember.labpipe.server.sessions.*

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
                        .find(EmailGroup::code `in` operator.notificationGroup, EmailGroup::formCode eq form.code).toMutableList()
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

    fun generateReportHtml(operator: Operator, form: FormTemplate, formData: JsonObject): String? {
        val reportTemplate = Runtime.mongoDatabase.getCollection<ReportTemplate>(RequiredMongoDBCollections.REPORT_TEMPLATES.value)
            .findOne (ReportTemplate::formCode eq form.code, ReportTemplate::active eq true )
        reportTemplate?.run {
            Runtime.logger.info { "Report template found." }
            Runtime.logger.info { reportTemplate.htmlTemplate.size }
            val elements = reportTemplate.htmlTemplate
            elements.sortedWith(compareBy { it.order })
            var reportBody = body()
            var validValueRegex = Regex("(.*)::(.*)")
            elements.forEach {
                run {
                    Runtime.logger.info { "Processing report element: $it" }
                    when (it.type) {
                        ReportElementTYPE.REPORT_TITLE.value -> {
                            val valueArray = it.value.split("::")
                            val value: String = when (valueArray[0]) {
                                ReportElementValueSource.STATIC_VALUE.value -> valueArray[1]
                                ReportElementValueSource.FORM_DATA.value -> ReportUtil.getFormDataAsString(formData, valueArray[1])
                                ReportElementValueSource.FORM_TEMPLATE.value -> form.getProperty(valueArray[1]) ?: "Unable to get ${valueArray[1]} from form template"
                                else -> "Invalid format in report config"
                            }
                            println("H1: $value")
                            reportBody.with(h1(value))
                        }
                        ReportElementTYPE.SECTION_TITLE.value -> {
                            val valueArray = it.value.split("::")
                            val value: String = when (valueArray[0]) {
                                ReportElementValueSource.STATIC_VALUE.value -> valueArray[1]
                                ReportElementValueSource.FORM_DATA.value -> ReportUtil.getFormDataAsString(formData, valueArray[1])
                                ReportElementValueSource.FORM_TEMPLATE.value -> form.getProperty(valueArray[1]) ?: "Unable to get valueArray[1] from form template"
                                else -> "Invalid format in report config"
                            }
                            println("H3: $value")
                            reportBody.with(h3(value))
                        }
                        ReportElementTYPE.TABLE_KEYVALUE.value -> {
                            val valueArray = it.value.split("::")
                            val value: JsonObject? = when (valueArray[0]) {
                                ReportElementValueSource.FORM_DATA.value -> formData.getAsJsonObject(valueArray[1])
                                else -> null
                            }
                            var table = table().withClasses("table", "table-responsive")
                            value?.run {
                                val entries = value.entrySet()
                                val chunkedEntries = entries.chunked(4)

                                chunkedEntries.forEach { chunk ->
                                    var row = tr()
                                    chunk.forEach {
                                        row.with(
                                            th(it.key), td(it.value.asString)
                                        )
                                    }
                                    table.with(row)
                                }
                            }
                            reportBody.with(table)
                        }
                        ReportElementTYPE.TABLE_LOOP.value -> {
                            println("To be implemented")
                            // TODO
                        }
                        ReportElementTYPE.LIST.value -> {
                            val valueArray = it.value.split("::")
                            val value: JsonArray? = when (valueArray[0]) {
                                ReportElementValueSource.FORM_DATA.value -> ReportUtil.getFormDataAsArray(formData, valueArray[1])
                                else -> null
                            }
                            var list = ol()
                            value?.run {
                                value.forEach { list.with(li(it.asString)) }
                            }
                            reportBody.with(list)
                        }
                        else -> reportBody.with(p("Invalid format in report config"))
                    }
                }
            }
            val report = html(
                head(
                    link().withRel("stylesheet").withHref("https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css")
                ),
                reportBody
            )
            val result = report.renderFormatted()
            println(result)
            return result
        }
        return null
    }

    fun generateReportText(operator: Operator, form: FormTemplate, formData: JsonObject) {
        // TODO
    }
}