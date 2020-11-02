package uk.ac.le.ember.labpipe.server.controllers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.litote.kmongo.`in`
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.simplejavamail.api.email.Recipient
import uk.ac.le.ember.labpipe.server.EmailGroup
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.NOTIFICATION
import uk.ac.le.ember.labpipe.server.Operator
import uk.ac.le.ember.labpipe.server.Record
import uk.ac.le.ember.labpipe.server.controllers.ConfigController.Companion.LabPipeConfig.Email
import uk.ac.le.ember.labpipe.server.sessions.Runtime

private val logger = KotlinLogging.logger {}


class NotificationController {
    companion object {
        fun sendNotificationEmail(operator: Operator, formIdentifier: String, record: JsonObject) {
            GlobalScope.launch {
                val recordForm = MONGO.COLLECTIONS.FORMS.findOne { FormTemplate::identifier eq formIdentifier }
                recordForm?.run {
                    val recipients = getEmailRecipients(operator, recordForm)
                    recipients?.run {
                        for (r in recipients) {
                            println("Recipient: ${r.name} <${r.address}>")
                        }
                        val htmlReport = ReportController.generateHtml(operator, recordForm, record)
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val textReport = gson.toJson(record)
                        EmailController.sendEmail(
                            from = Recipient(
                                Runtime.config[Email.fromName],
                                Runtime.config[Email.fromAddress],
                                null
                            ),
                            to = recipients,
                            subject = recordForm.notificationSubject,
                            text = textReport,
                            html = htmlReport ?: textReport,
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
                        val htmlReport = ReportController.generateHtml(operator, recordForm, recordObject)
                        EmailController.sendEmail(
                            from = Recipient(
                                Runtime.config[Email.fromName],
                                Runtime.config[Email.fromAddress],
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
            logger.info { "Form [${form?.identifier}] requests notification style: ${form?.notificationStyle}" }
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
}