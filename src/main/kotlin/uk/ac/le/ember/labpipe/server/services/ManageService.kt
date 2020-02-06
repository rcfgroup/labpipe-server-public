package uk.ac.le.ember.labpipe.server.services

import com.github.ajalt.clikt.output.TermUi.echo
import io.javalin.core.security.SecurityUtil.roles
import io.javalin.http.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.RandomStringUtils
import org.litote.kmongo.*
import org.mindrot.jbcrypt.BCrypt
import org.simplejavamail.api.email.Recipient
import uk.ac.le.ember.labpipe.server.*
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.notification.NotificationUtil
import uk.ac.le.ember.labpipe.server.notification.ReportUtil
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.util.*


fun addOperator(email: String, name: String, notify: Boolean = true, show: Boolean = false): ResultMessage {
    val current = MONGO.COLLECTIONS.OPERATORS.findOne(Operator::email eq email)
    current?.run {
        return ResultMessage(
            400,
            Message("""Operator with email [$email] already exists.""")
        )
    }
    val operator = Operator(email = email)
    operator.name = name
    operator.username = email
    val tempPassword = RandomStringUtils.randomAlphanumeric(8)
    operator.passwordHash = BCrypt.hashpw(tempPassword, BCrypt.gensalt())
    operator.active = true
    operator.roles.add(DEFAULT_OPERATOR_ROLE.identifier)
    MONGO.COLLECTIONS.OPERATORS.insertOne(operator)
    if (show) {
        echo("[Username] ${operator.username}")
        echo("[Password] $tempPassword")
    }
    operator.notificationGroup.forEach {
        MONGO.COLLECTIONS.EMAIL_GROUPS.updateOne(
            EmailGroup::identifier eq it,
            EmailGroup::member addToSet operator.username
        )
    }
    if (notify) {
        EmailUtil.sendEmail(
            from = Recipient(
                Runtime.lpConfig.notificationEmailName,
                Runtime.lpConfig.notificationEmailAddress,
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
    return ResultMessage(
        200,
        Message(MESSAGES.OPERATOR_ADDED)
    )
}

fun addOperator(operator: Operator, notify: Boolean = true, show: Boolean = false): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<Operator>(MONGO.COL_NAMES.OPERATORS)
    val current = col.findOne(Operator::email eq operator.email)
    current?.run {
        return ResultMessage(
            400,
            Message("""Operator with email [$email] already exists.""")
        )
    }
    val tempPassword = RandomStringUtils.randomAlphanumeric(8)
    operator.passwordHash = BCrypt.hashpw(tempPassword, BCrypt.gensalt())
    operator.active = true
    operator.roles.add(DEFAULT_OPERATOR_ROLE.identifier)
    col.insertOne(operator)
    if (show) {
        echo("[Username] ${operator.username}")
        echo("[Password] $tempPassword")
    }
    operator.notificationGroup.forEach {
        MONGO.COLLECTIONS.EMAIL_GROUPS.updateOne(
            EmailGroup::identifier eq it,
            EmailGroup::member addToSet operator.username
        )
    }
    if (notify) {
        EmailUtil.sendEmail(
            from = Recipient(
                Runtime.lpConfig.notificationEmailName,
                Runtime.lpConfig.notificationEmailAddress,
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
    return ResultMessage(
        200,
        Message(MESSAGES.OPERATOR_ADDED)
    )
}

private fun addOperator(ctx: Context): Context {
    val operator = ctx.body<Operator>()
    val result = addOperator(operator, true)
    return ctx.status(result.status).json(result.message)
}

fun changePassword(operator: Operator, newPassHash: String): ResultMessage {
    val decoder = Base64.getDecoder()
    val decoded = decoder.decode(newPassHash)
    val newPassword = String(decoded, Charsets.UTF_8)
    operator.passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
    MONGO.COLLECTIONS.OPERATORS.updateOne(Operator::username eq operator.username, operator)
    return ResultMessage(200, Message("Password updated."))
}

private fun changePassword(ctx: Context): Context {
    val operator = AuthManager.getUser(ctx)
    val newPassHash = ctx.body()
    operator?.run {
        val result = changePassword(operator, newPassHash)
        return ctx.status(200).json(result.message)
    }
    return ctx.status(400).json(Message(MESSAGES.UNAUTHORIZED))
}

fun addToken(operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<AccessToken>(MONGO.COL_NAMES.ACCESS_TOKENS)
    var token = UUID.randomUUID().toString()
    while (col.findOne(AccessToken::token eq token) != null
    ) {
        token = UUID.randomUUID().toString()
    }
    val key = RandomStringUtils.randomAlphanumeric(16)
    val accessToken =
        AccessToken(token = token, keyHash = BCrypt.hashpw(key, BCrypt.gensalt()))
    accessToken.roles.add(DEFAULT_TOKEN_ROLE.identifier)
    col.insertOne(accessToken)
    echo("""[Token] $token""")
    echo("""[Key] $key""")
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
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
    return ResultMessage(
        200,
        Message(MESSAGES.TOKEN_ADDED)
    )
}

private fun addToken(ctx: Context): Context {
    val operator = AuthManager.getUser(ctx)
    val result = addToken(operator = operator, notify = true)
    return ctx.status(result.status).json(result.message)
}

fun addRole(identifier: String, name: String, operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val current =
        Runtime.mongoDatabase.getCollection<OperatorRole>(MONGO.COL_NAMES.ROLES)
            .findOne(OperatorRole::identifier eq identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Role with identifier [$identifier] already exists.""")
        )
    }
    val role = OperatorRole(identifier = identifier, name = name)
    Runtime.mongoDatabase.getCollection<OperatorRole>(MONGO.COL_NAMES.ROLES)
        .insertOne(role)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
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
    return ResultMessage(
        200,
        Message(MESSAGES.ROLE_ADDED)
    )
}

fun addRole(role: OperatorRole, operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<OperatorRole>(MONGO.COL_NAMES.ROLES)
    val current = col.findOne(OperatorRole::identifier eq role.identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Role with identifier [$identifier] already exists.""")
        )
    }
    col.insertOne(role)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
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
                text = String.format(EmailTemplates.CREATE_ROLE_TEXT, role.identifier, name),
                html = String.format(EmailTemplates.CREATE_ROLE_HTML, role.identifier, name),
                async = true
            )
        }
    }
    return ResultMessage(
        200,
        Message(MESSAGES.ROLE_ADDED)
    )
}

private fun addRole(ctx: Context): Context {
    val role = ctx.body<OperatorRole>()
    val operator = AuthManager.getUser(ctx)
    val result = addRole(role = role, operator = operator, notify = true)
    return ctx.status(result.status).json(result.message)
}

fun addEmailGroup(
    identifier: String,
    name: String,
    formIdentifier: String,
    operator: Operator? = null,
    notify: Boolean = true
): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<EmailGroup>(MONGO.COL_NAMES.EMAIL_GROUPS)
    val current = col.findOne(EmailGroup::identifier eq identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Email group with identifier [$identifier] already exists.""")
        )
    }
    val group = EmailGroup(identifier = identifier)
    group.name = name
    group.formIdentifier = formIdentifier
    col.insertOne(group)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
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
    return ResultMessage(
        200,
        Message(MESSAGES.EMAIL_GROUP_ADDED)
    )
}

fun addEmailGroup(emailGroup: EmailGroup, operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<EmailGroup>(MONGO.COL_NAMES.EMAIL_GROUPS)
    val current = col.findOne(EmailGroup::identifier eq emailGroup.identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Email group with identifier [$identifier] already exists.""")
        )
    }
    col.insertOne(emailGroup)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
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
                text = String.format(
                    EmailTemplates.CREATE_EMAILGROUP_TEXT,
                    emailGroup.identifier,
                    emailGroup.name,
                    emailGroup.formIdentifier
                ),
                html = String.format(
                    EmailTemplates.CREATE_EMAILGROUP_HTML,
                    emailGroup.identifier,
                    emailGroup.name,
                    emailGroup.formIdentifier
                ),
                async = true
            )
        }
    }
    return ResultMessage(
        200,
        Message(MESSAGES.EMAIL_GROUP_ADDED)
    )
}

private fun addEmailGroup(ctx: Context): Context {
    val emailGroup = ctx.body<EmailGroup>()
    val operator = AuthManager.getUser(ctx)
    val result = addEmailGroup(emailGroup = emailGroup, operator = operator, notify = true)
    return ctx.status(result.status).json(result.message)
}

fun addInstrument(
    identifier: String,
    name: String,
    realtime: Boolean = false,
    fileType: MutableList<String> = mutableListOf(),
    operator: Operator? = null,
    notify: Boolean = true
): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<Instrument>(MONGO.COL_NAMES.INSTRUMENTS)
    val current = col.findOne(Instrument::identifier eq identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Instrument with identifier [$identifier] already exists.""")
        )
    }
    val instrument = Instrument(identifier = identifier, name = name)
    instrument.realtime = realtime
    instrument.fileType = fileType.toMutableSet()
    col.insertOne(instrument)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
                    null
                ),
                to = listOf(
                    Recipient(
                        operator.name,
                        operator.email,
                        null
                    )
                ),
                subject = "LabPipe Instrument Added",
                text = String.format(EmailTemplates.CREATE_INSTRUMENT_TEXT, identifier, name),
                html = String.format(EmailTemplates.CREATE_INSTRUMENT_HTML, identifier, name),
                async = true
            )
        }
    }
    return ResultMessage(
        200,
        Message(MESSAGES.INSTRUMENT_ADDED)
    )
}

fun addInstrument(instrument: Instrument, operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<Instrument>(MONGO.COL_NAMES.INSTRUMENTS)
    val current = col.findOne(Instrument::identifier eq instrument.identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Instrument with identifier [${instrument.identifier}] already exists.""")
        )
    }
    col.insertOne(instrument)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
                    null
                ),
                to = listOf(
                    Recipient(
                        operator.name,
                        operator.email,
                        null
                    )
                ),
                subject = "LabPipe Instrument Added",
                text = String.format(EmailTemplates.CREATE_INSTRUMENT_TEXT, instrument.identifier, name),
                html = String.format(EmailTemplates.CREATE_INSTRUMENT_HTML, instrument.identifier, name),
                async = true
            )
        }
    }
    return ResultMessage(
        200,
        Message(MESSAGES.INSTRUMENT_ADDED)
    )
}

private fun addInstrument(ctx: Context): Context {
    val instrument = ctx.body<Instrument>()
    val operator = AuthManager.getUser(ctx)
    val result = addInstrument(instrument, operator, true)
    return ctx.status(result.status).json(result.message)
}

fun addLocation(location: Location, operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<Location>(MONGO.COL_NAMES.LOCATIONS)
    val current = col.findOne(Location::identifier eq location.identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Location with identifier [${location.identifier}] already exists.""")
        )
    }
    col.insertOne(location)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
                    null
                ),
                to = listOf(
                    Recipient(
                        operator.name,
                        operator.email,
                        null
                    )
                ),
                subject = "LabPipe Location Added",
                text = String.format(EmailTemplates.CREATE_LOCATION_TEXT, location.identifier, name),
                html = String.format(EmailTemplates.CREATE_LOCATION_HTML, location.identifier, name),
                async = true
            )
        }
    }
    return ResultMessage(
        200,
        Message(MESSAGES.LOCATION_ADDED)
    )
}

private fun addLocation(ctx: Context): Context {
    val location = ctx.body<Location>()
    val operator = AuthManager.getUser(ctx)
    val result = addLocation(location, operator, true)
    return ctx.status(result.status).json(result.message)
}

fun addStudy(study: Study, operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<Study>(MONGO.COL_NAMES.STUDIES)
    val current = col.findOne(Study::identifier eq study.identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Study with identifier [${study.identifier}] already exists.""")
        )
    }
    col.insertOne(study)
    operator?.run {
        if (notify) {
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.lpConfig.notificationEmailName,
                    Runtime.lpConfig.notificationEmailAddress,
                    null
                ),
                to = listOf(
                    Recipient(
                        operator.name,
                        operator.email,
                        null
                    )
                ),
                subject = "LabPipe Study Added",
                text = String.format(EmailTemplates.CREATE_STUDY_TEXT, study.identifier, name),
                html = String.format(EmailTemplates.CREATE_STUDY_HTML, study.identifier, name),
                async = true
            )
        }
    }
    return ResultMessage(
        200,
        Message(MESSAGES.STUDY_ADDED)
    )
}

private fun addStudy(ctx: Context): Context {
    val study = ctx.body<Study>()
    val operator = AuthManager.getUser(ctx)
    val result = addStudy(study, operator, true)
    return ctx.status(result.status).json(result.message)
}

fun manageRoutes() {
    println("Add manage service routes.")
    Runtime.server.post(
        API.MANAGE.CREATE.OPERATOR, { ctx -> addOperator(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.put(
        API.MANAGE.UPDATE.PASSWORD, { ctx -> changePassword(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.post(
        API.MANAGE.CREATE.TOKEN, { ctx -> addToken(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.post(
        API.MANAGE.CREATE.ROLE, { ctx -> addRole(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.post(
        API.MANAGE.CREATE.EMAIL_GROUP, { ctx -> addEmailGroup(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.post(
        API.MANAGE.CREATE.INSTRUMENT, { ctx -> addInstrument(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.post(
        API.MANAGE.CREATE.LOCATION, { ctx -> addLocation(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.post(
        API.MANAGE.CREATE.STUDY, { ctx -> addStudy(ctx) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
}