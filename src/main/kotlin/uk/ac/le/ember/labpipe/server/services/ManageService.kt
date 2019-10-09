package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import io.javalin.http.Context
import org.apache.commons.lang3.RandomStringUtils
import org.litote.kmongo.*
import org.mindrot.jbcrypt.BCrypt
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.EmailTemplates
import uk.ac.le.ember.labpipe.server.data.*
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.util.*

object ManageService {
    fun addOperator(email: String, name: String, notify: Boolean = true): ResultMessage {
        val col = Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
        val current = col.findOne(Operator::email eq email)
        current?.run {
            return ResultMessage(400, Message("""Operator with email [$email] already exists."""))
        }
        val operator = Operator(email = email)
        operator.name = name
        operator.username = email
        val tempPassword = RandomStringUtils.randomAlphanumeric(8)
        operator.passwordHash = BCrypt.hashpw(tempPassword, BCrypt.gensalt())
        operator.active = true
        Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
            .insertOne(operator)
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.config.notificationEmailName,
                    Runtime.config.notificationEmailAddress,
                    null
                ),
                to = listOf(
                    Recipient(
                        operator.name,
                        operator.email,
                        null
                    )
                ),
                subject = "Your LabPipe Operator Account",
                text = String.format(
                    EmailTemplates.CREATE_OPERATOR_TEXT,
                    operator.name,
                    operator.email,
                    tempPassword
                ),
                html = String.format(
                    EmailTemplates.CREATE_OPERATOR_HTML,
                    operator.name,
                    operator.email,
                    tempPassword
                ),
                async = true
            )
        }
        return ResultMessage(200, Message(Constants.MESSAGES.OPERATOR_CREATED))
    }

    private fun addOperator(ctx: Context): Context {
        val email = ctx.queryParam("email")
        val name = ctx.queryParam("name")
        email?.run {
            name?.run {
                val result = addOperator(email = email, name = name, notify = true)
                return ctx.status(result.status).json(result.message)
            }
        }
        return ctx.status(400).json(Message("Please make sure you have provided name and email."))
    }

    fun addToken(operator: Operator? = null, notify: Boolean = true): ResultMessage {
        var token = UUID.randomUUID().toString()
        while (Runtime.mongoDatabase.getCollection<AccessToken>(Constants.MONGO.REQUIRED_COLLECTIONS.ACCESS_TOKENS)
                .findOne(AccessToken::token eq token) != null
        ) {
            token = UUID.randomUUID().toString()
        }
        val key = RandomStringUtils.randomAlphanumeric(16)
        val accessToken = AccessToken(token = token, keyHash = BCrypt.hashpw(key, BCrypt.gensalt()))
        Runtime.mongoDatabase.getCollection<AccessToken>(Constants.MONGO.REQUIRED_COLLECTIONS.ACCESS_TOKENS)
            .insertOne(accessToken)
        operator?.run {
            if (notify) {
                EmailUtil.sendEmail(
                    from = Recipient(
                        Runtime.config.notificationEmailName,
                        Runtime.config.notificationEmailAddress,
                        null
                    ),
                    to = listOf(
                        Recipient(
                            operator.name,
                            operator.email,
                            null
                        )
                    ),
                    subject = "LabPipe Access Token Created",
                    text = String.format(EmailTemplates.CREATE_TOKEN_TEXT, token, key),
                    html = String.format(EmailTemplates.CREATE_TOKEN_HTML, token, key),
                    async = true
                )
            }
        }
        return ResultMessage(200, Message(Constants.MESSAGES.TOKEN_CREATED))
    }

    private fun addToken(ctx: Context): Context {
        val operator = AuthManager.getUser(ctx)
        val result = addToken(operator = operator, notify = true)
        return ctx.status(result.status).json(result.message)
    }

    fun addRole(identifier: String, name: String, operator: Operator? = null, notify: Boolean = true): ResultMessage {
        val current =
            Runtime.mongoDatabase.getCollection<OperatorRole>(Constants.MONGO.REQUIRED_COLLECTIONS.ROLES)
                .findOne(OperatorRole::identifier eq identifier)
        current?.run {
            return ResultMessage(400, Message("""Role with identifier [$identifier] already exists."""))
        }
        val role = OperatorRole(identifier = identifier, name = name)
        Runtime.mongoDatabase.getCollection<OperatorRole>(Constants.MONGO.REQUIRED_COLLECTIONS.ROLES)
            .insertOne(role)
        operator?.run {
            if (notify) {
                EmailUtil.sendEmail(
                    from = Recipient(
                        Runtime.config.notificationEmailName,
                        Runtime.config.notificationEmailAddress,
                        null
                    ),
                    to = listOf(
                        Recipient(
                            operator.name,
                            operator.email,
                            null
                        )
                    ),
                    subject = "LabPipe Role Created",
                    text = String.format(EmailTemplates.CREATE_ROLE_TEXT, identifier, name),
                    html = String.format(EmailTemplates.CREATE_ROLE_HTML, identifier, name),
                    async = true
                )
            }
        }
        return ResultMessage(200, Message(Constants.MESSAGES.ROLE_CREATED))
    }

    private fun addRole(ctx: Context): Context {
        val identifier = ctx.queryParam("identifier")
        val name = ctx.queryParam("name")
        identifier?.run {
            name?.run {
                val operator = AuthManager.getUser(ctx)
                val result = addRole(identifier = identifier, name = name, operator = operator, notify = true)
                return ctx.status(result.status).json(result.message)
            }
        }
        return ctx.status(400).json(Message("Please make sure you have provided name and email."))
    }

    fun addEmailGroup(
        identifier: String,
        name: String,
        formIdentifier: String,
        operator: Operator?,
        notify: Boolean = true
    ): ResultMessage {
        val col = Runtime.mongoDatabase.getCollection<EmailGroup>(Constants.MONGO.REQUIRED_COLLECTIONS.EMAIL_GROUPS)
        val current = col.findOne(EmailGroup::identifier eq identifier)
        current?.run {
            return ResultMessage(400, Message("""Email group with identifier [$identifier] already exists."""))
        }
        val group = EmailGroup(identifier = identifier)
        group.name = name
        group.formIdentifier = formIdentifier
        Runtime.mongoDatabase.getCollection<EmailGroup>(Constants.MONGO.REQUIRED_COLLECTIONS.EMAIL_GROUPS)
            .insertOne(group)
        operator?.run {
            if (notify) {
                EmailUtil.sendEmail(
                    from = Recipient(
                        Runtime.config.notificationEmailName,
                        Runtime.config.notificationEmailAddress,
                        null
                    ),
                    to = listOf(
                        Recipient(
                            operator.name,
                            operator.email,
                            null
                        )
                    ),
                    subject = "LabPipe Role Created",
                    text = String.format(EmailTemplates.CREATE_EMAILGROUP_TEXT, identifier, name, formIdentifier),
                    html = String.format(EmailTemplates.CREATE_EMAILGROUP_HTML, identifier, name, formIdentifier),
                    async = true
                )
            }
        }
        return ResultMessage(200, Message(Constants.MESSAGES.EMAIL_GROUP_CREATED))
    }

    private fun addEmailGroup(ctx: Context): Context {
        val identifier = ctx.queryParam("identifier")
        val name = ctx.queryParam("name")
        val formIdentifier = ctx.queryParam("formIdentifier")
        identifier?.run {
            name?.run {
                formIdentifier?.run {
                    val operator = AuthManager.getUser(ctx)
                    val result = addEmailGroup(identifier = identifier, name = name, formIdentifier = formIdentifier, operator = operator, notify = true)
                    return ctx.status(result.status).json(result.message)
                }
            }
        }
        return ctx.status(400).json(Message("Missing required parameter."))
    }

    fun addInstrument(identifier: String, name: String, realtime: Boolean = false, fileType: MutableList<String> = mutableListOf(), operator: Operator?, notify: Boolean = true): ResultMessage {
        val col = Runtime.mongoDatabase.getCollection<Instrument>(Constants.MONGO.REQUIRED_COLLECTIONS.INSTRUMENTS)
        val current = col.findOne(Instrument::identifier eq identifier)
        current?.run {
            return ResultMessage(400, Message("""Instrument with identifier [$identifier] already exists."""))
        }
        val instrument = Instrument(identifier = identifier, name = name)
        instrument.realtime = realtime
        instrument.fileType = fileType
        col.insertOne(instrument)
        operator?.run {
            if (notify) {
                EmailUtil.sendEmail(
                    from = Recipient(
                        Runtime.config.notificationEmailName,
                        Runtime.config.notificationEmailAddress,
                        null
                    ),
                    to = listOf(
                        Recipient(
                            operator.name,
                            operator.email,
                            null
                        )
                    ),
                    subject = "LabPipe Role Created",
                    text = String.format(EmailTemplates.CREATE_INSTRUMENT_TEXT, identifier, name),
                    html = String.format(EmailTemplates.CREATE_INSTRUMENT_HTML, identifier, name),
                    async = true
                )
            }
        }
        return ResultMessage(200, Message(Constants.MESSAGES.INSTRUMENT_CREATED))
    }

    fun routes() {
        println("Add manage service routes.")
        Runtime.server.get(
            Constants.API.MANAGE.CREATE.OPERATOR, { ctx -> addOperator(ctx) },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            Constants.API.MANAGE.CREATE.TOKEN, { ctx -> addToken(ctx) },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            Constants.API.MANAGE.CREATE.ROLE, { ctx -> addRole(ctx) },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            Constants.API.MANAGE.CREATE.EMAIL_GROUP, { ctx -> addEmailGroup(ctx) },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
    }
}