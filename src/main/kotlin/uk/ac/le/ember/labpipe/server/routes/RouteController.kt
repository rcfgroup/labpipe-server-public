package uk.ac.le.ember.labpipe.server.routes

import com.google.gson.JsonParser
import io.javalin.core.security.SecurityUtil
import io.javalin.http.Context
import io.javalin.http.util.RateLimit
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.API
import uk.ac.le.ember.labpipe.server.ApiAccessRole
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.DB_COL_FORM_DATA_PREFIX
import uk.ac.le.ember.labpipe.server.MESSAGES
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.Message
import uk.ac.le.ember.labpipe.server.Record
import uk.ac.le.ember.labpipe.server.controllers.ConfigController
import uk.ac.le.ember.labpipe.server.controllers.NotificationController
import uk.ac.le.ember.labpipe.server.services.findOneInstrument
import uk.ac.le.ember.labpipe.server.services.findOneStudy
import uk.ac.le.ember.labpipe.server.services.getForm
import uk.ac.le.ember.labpipe.server.services.getParameter
import uk.ac.le.ember.labpipe.server.services.listForms
import uk.ac.le.ember.labpipe.server.services.listInstruments
import uk.ac.le.ember.labpipe.server.services.listRecords
import uk.ac.le.ember.labpipe.server.services.listStudies
import uk.ac.le.ember.labpipe.server.services.saveRecord
import uk.ac.le.ember.labpipe.server.services.uploadFile
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

class RouteController {
    companion object {
        fun addRoutes() {
            addGeneralRoutes()
            addFormRoutes()
            addParameterRoutes()
            addQueryRoutes()
            addRecordRoutes()
            addManageRoutes()
            addUploadRoutes()
        }

        private fun getRateLimiter(url: String? = null, ctx: Context, public: Boolean = false) {
            if (public) {
                RateLimit(ctx).requestPerTimeUnit(Runtime.config[ConfigController.Companion.LabPipeConfig.Security.rateLimitPublic], TimeUnit.MINUTES)
            }else {
                if (!url.isNullOrEmpty()) {
                    val apiAccessRole = MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq url)
                    if (apiAccessRole != null && apiAccessRole.rate > 0) {
                        RateLimit(ctx).requestPerTimeUnit(apiAccessRole.rate, TimeUnit.MINUTES)
                    }
                }
            }
        }

        fun addGeneralRoutes() {
            Runtime.server.get(
                API.ROOT,
                { ctx ->
                    getRateLimiter(ctx = ctx, public = true)
                    ctx.json(Message(MESSAGES.SERVER_RUNNING)) },
                SecurityUtil.roles(
                    AuthManager.ApiRole.PUBLIC,
                    AuthManager.ApiRole.AUTHORISED,
                    AuthManager.ApiRole.TOKEN_AUTHORISED,
                    AuthManager.ApiRole.UNAUTHORISED
                )
            )
            Runtime.server.get(
                API.GENERAL.CONN_PUBLIC,
                { ctx ->
                    getRateLimiter(ctx = ctx, public = true)
                    ctx.json(Message(MESSAGES.CONN_PUBLIC_SUCCESS)) },
                SecurityUtil.roles(AuthManager.ApiRole.PUBLIC)
            )
            Runtime.server.get(
                API.GENERAL.CONN_AUTH,
                { ctx ->
                    getRateLimiter(ctx = ctx, url = API.GENERAL.CONN_AUTH)
                    ctx.json(Message(MESSAGES.CONN_AUTH_SUCCESS)) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.get(
                API.GENERAL.CONN_TOKEN,
                { ctx ->
                    getRateLimiter(ctx = ctx, url = API.GENERAL.CONN_TOKEN)
                    ctx.json(Message(MESSAGES.CONN_TOKEN_SUCCESS)) },
                SecurityUtil.roles(AuthManager.ApiRole.TOKEN_AUTHORISED)
            )
        }

        fun addFormRoutes() {
            Runtime.server.get(
                API.FORM.ALL,
                { ctx ->
                    getRateLimiter(ctx = ctx, url = API.FORM.ALL)
                    ctx.json(listForms()) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
            )
            Runtime.server.get(
                API.FORM.FROM_IDENTIFIER,
                { ctx ->
                    getRateLimiter(ctx = ctx, url = API.FORM.FROM_IDENTIFIER)
                    getForm(ctx.pathParam("identifier"))?.let { ctx.json(it) }
                },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
            )
            Runtime.server.get(
                API.FORM.FROM_STUDY_INSTRUMENT,
                { ctx ->
                    getRateLimiter(ctx = ctx, url = API.FORM.FROM_STUDY_INSTRUMENT)
                    ctx.json(getForm(ctx.pathParam("studyIdentifier"), ctx.pathParam("instrumentIdentifier"))) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
            )
        }

        fun addParameterRoutes() {
            Runtime.server.get(
                API.PARAMETER.FROM_NAME, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.PARAMETER.FROM_NAME)
                    ctx.json(getParameter(ctx.pathParam("identifier"))) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
            )
        }

        fun addQueryRoutes() {
            Runtime.server.get(
                API.QUERY.RECORDS, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.QUERY.RECORDS)
                    ctx.json(listRecords()) },
                SecurityUtil.roles(
                    AuthManager.ApiRole.AUTHORISED,
                    AuthManager.ApiRole.TOKEN_AUTHORISED
                )
            )
            Runtime.server.get(
                API.QUERY.STUDY_RECORDS, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.QUERY.STUDY_RECORDS)
                    ctx.json(listRecords(ctx.pathParam("studyIdentifier"))) },
                SecurityUtil.roles(
                    AuthManager.ApiRole.AUTHORISED,
                    AuthManager.ApiRole.TOKEN_AUTHORISED
                )
            )
            Runtime.server.get(
                API.QUERY.STUDIES, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.QUERY.STUDIES)
                    ctx.json(listStudies()) },
                SecurityUtil.roles(
                    AuthManager.ApiRole.AUTHORISED,
                    AuthManager.ApiRole.TOKEN_AUTHORISED
                )
            )
            Runtime.server.get(
                API.QUERY.STUDY, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.QUERY.STUDY)
                    findOneStudy(ctx) },
                SecurityUtil.roles(
                    AuthManager.ApiRole.AUTHORISED,
                    AuthManager.ApiRole.TOKEN_AUTHORISED
                )
            )
            Runtime.server.get(
                API.QUERY.INSTRUMENTS, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.QUERY.INSTRUMENTS)
                    ctx.json(listInstruments()) },
                SecurityUtil.roles(
                    AuthManager.ApiRole.AUTHORISED,
                    AuthManager.ApiRole.TOKEN_AUTHORISED
                )
            )
            Runtime.server.get(
                API.QUERY.INSTRUMENT, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.QUERY.INSTRUMENT)
                    findOneInstrument(ctx) },
                SecurityUtil.roles(
                    AuthManager.ApiRole.AUTHORISED,
                    AuthManager.ApiRole.TOKEN_AUTHORISED
                )
            )
        }

        fun addRecordRoutes() {
            Runtime.server.post(API.RECORD.ADD, { ctx ->
                getRateLimiter(ctx = ctx, url = API.RECORD.ADD)
                val operator = AuthManager.getUser(ctx)
                operator?.run {
                    val record = JsonParser.parseString(ctx.body()).asJsonObject
                    val formIdentifier = record.get("formIdentifier").asString
                    record.addProperty("uploaded_by", operator.username)
                    record.addProperty("created", LocalDateTime.now().toString())
                    val colRecord = Runtime.mongoDatabase.getCollection<Record>("$DB_COL_FORM_DATA_PREFIX${formIdentifier}")
                    val current = colRecord.findOne(Record::actionIdentifier eq record.get("actionIdentifier").asString)
                    if (current != null) {
                        logger.info { "Record exists [${current.actionIdentifier}]" }
                        NotificationController.sendNotificationEmail(operator, formIdentifier, record)
                        ctx.status(200)
                        ctx.json(
                            Message("Record found on server. Resent notification.")
                        )
                    } else {
                        val recordId = saveRecord(record)
                        if (recordId != null) {
                            logger.info { "Record saved [$recordId]" }
                            NotificationController.sendNotificationEmail(operator, formIdentifier, record)
                            ctx.status(200)
                        } else {
                            ctx.status(500)
                            ctx.json(
                                Message("Record cannot be saved. Please retry or contact service manager.")
                            )
                        }
                    }
                }
            }, SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED))
        }

        fun addManageRoutes() {
            Runtime.server.post(
                API.MANAGE.CREATE.OPERATOR, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.CREATE.OPERATOR)
                    uk.ac.le.ember.labpipe.server.services.addOperator(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.put(
                API.MANAGE.UPDATE.PASSWORD, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.UPDATE.PASSWORD)
                    uk.ac.le.ember.labpipe.server.services.changePassword(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.post(
                API.MANAGE.CREATE.TOKEN, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.CREATE.TOKEN)
                    uk.ac.le.ember.labpipe.server.services.addToken(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.post(
                API.MANAGE.CREATE.ROLE, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.CREATE.ROLE)
                    uk.ac.le.ember.labpipe.server.services.addRole(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.post(
                API.MANAGE.CREATE.EMAIL_GROUP, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.CREATE.EMAIL_GROUP)
                    uk.ac.le.ember.labpipe.server.services.addEmailGroup(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.post(
                API.MANAGE.CREATE.INSTRUMENT, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.CREATE.INSTRUMENT)
                    uk.ac.le.ember.labpipe.server.services.addInstrument(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.post(
                API.MANAGE.CREATE.LOCATION, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.CREATE.LOCATION)
                    uk.ac.le.ember.labpipe.server.services.addLocation(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
            Runtime.server.post(
                API.MANAGE.CREATE.STUDY, { ctx ->
                    getRateLimiter(ctx = ctx, url = API.MANAGE.CREATE.STUDY)
                    uk.ac.le.ember.labpipe.server.services.addStudy(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED)
            )
        }

        fun addUploadRoutes() {
            Runtime.server.post(API.UPLOAD.FORM_FILE, { ctx ->
                getRateLimiter(ctx = ctx, url = API.UPLOAD.FORM_FILE)
                uploadFile(ctx) },
                SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED))
        }
    }
}