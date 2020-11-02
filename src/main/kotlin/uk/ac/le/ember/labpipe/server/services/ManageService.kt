package uk.ac.le.ember.labpipe.server.services

import com.github.ajalt.clikt.output.TermUi.echo
import io.javalin.http.Context
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne
import org.mindrot.jbcrypt.BCrypt
import org.simplejavamail.api.email.Recipient
import uk.ac.le.ember.labpipe.server.AccessToken
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.DEFAULT_OPERATOR_ROLE
import uk.ac.le.ember.labpipe.server.DEFAULT_TOKEN_ROLE
import uk.ac.le.ember.labpipe.server.EmailGroup
import uk.ac.le.ember.labpipe.server.EmailTemplates
import uk.ac.le.ember.labpipe.server.Instrument
import uk.ac.le.ember.labpipe.server.Location
import uk.ac.le.ember.labpipe.server.MESSAGES
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.Message
import uk.ac.le.ember.labpipe.server.Operator
import uk.ac.le.ember.labpipe.server.OperatorRole
import uk.ac.le.ember.labpipe.server.ResultMessage
import uk.ac.le.ember.labpipe.server.Study
import uk.ac.le.ember.labpipe.server.controllers.ConfigController.Companion.LabPipeConfig.Email
import uk.ac.le.ember.labpipe.server.controllers.EmailController
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.util.Base64
import java.util.UUID

private val logger = KotlinLogging.logger {}


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
    postAddOperator(operator, tempPassword, show, notify)
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
    postAddOperator(operator, tempPassword, show, notify)
    return ResultMessage(
        200,
        Message(MESSAGES.OPERATOR_ADDED)
    )
}

fun postAddOperator(operator: Operator, tempPassword:String,  show: Boolean, notify: Boolean) {
    if (show) {
        logger.debug{"[Username] ${operator.username}" }
        logger.debug { "[Password] $tempPassword" }
    }
    operator.notificationGroup.forEach {
        MONGO.COLLECTIONS.EMAIL_GROUPS.updateOne(
            EmailGroup::identifier eq it,
            addToSet(EmailGroup::member, operator.username)
        )
    }
    if (notify && Runtime.emailAvailable) {
        EmailController.sendEmail(
            from = Recipient(
                Runtime.config[Email.fromName],
                Runtime.config[Email.fromAddress],
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
}

fun addOperator(ctx: Context): Context {
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

fun changePassword(ctx: Context): Context {
    val operator = AuthManager.getUser(ctx)
    val newPassHash = ctx.body()
    operator?.run {
        val result = changePassword(operator, newPassHash)
        return ctx.status(200).json(result.message)
    }
    return ctx.status(400).json(Message(MESSAGES.UNAUTHORIZED))
}

fun generateTokenAndKey(): Pair<String, String> {
    return Pair(UUID.randomUUID().toString(), RandomStringUtils.randomAlphanumeric(16))
}

fun addToken(operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<AccessToken>(MONGO.COL_NAMES.ACCESS_TOKENS)
    var (token, key) = generateTokenAndKey()
    while (col.findOne(AccessToken::token eq token) != null) {
        val (newToken, newKey) = generateTokenAndKey()
        token = newToken
        key = newKey
    }
    val accessToken =
        AccessToken(token = token, keyHash = BCrypt.hashpw(key, BCrypt.gensalt()))
    accessToken.roles.add(DEFAULT_TOKEN_ROLE.identifier)
    col.insertOne(accessToken)
    echo("""[Token] $token""")
    echo("""[Key] $key""")
    operator?.run {
        if (notify) {
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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

fun addToken(ctx: Context): Context {
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
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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

fun addRole(ctx: Context): Context {
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
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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

fun addEmailGroup(ctx: Context): Context {
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
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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

fun addInstrument(ctx: Context): Context {
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
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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

fun addLocation(ctx: Context): Context {
    val location = ctx.body<Location>()
    val operator = AuthManager.getUser(ctx)
    val result = addLocation(location, operator, true)
    return ctx.status(result.status).json(result.message)
}

fun addStudy(study: Study, config: String? = null, operator: Operator? = null, notify: Boolean = true): ResultMessage {
    val col = Runtime.mongoDatabase.getCollection<Study>(MONGO.COL_NAMES.STUDIES)
    val current = col.findOne(Study::identifier eq study.identifier)
    current?.run {
        return ResultMessage(
            400,
            Message("""Study with identifier [${study.identifier}] already exists.""")
        )
    }
    col.insertOne(study)
    config?.run {
        val dollarSign = "$"
        col.updateOne("""{identifier: '${study.identifier}'}""", """{${dollarSign}set:{config:${config}}}""")
    }
    operator?.run {
        if (notify) {
            EmailController.sendEmail(
                from = Recipient(
                    Runtime.config[Email.fromName],
                    Runtime.config[Email.fromAddress],
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

fun addStudy(ctx: Context): Context {
    val study = ctx.body<Study>()
    val operator = AuthManager.getUser(ctx)
    val result = addStudy(study, operator = operator, notify = true)
    return ctx.status(result.status).json(result.message)
}