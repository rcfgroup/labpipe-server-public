package uk.ac.le.ember.labpipe.server.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.javalin.core.security.SecurityUtil
import org.bson.Document
import org.bson.types.ObjectId
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.notification.NotificationUtil
import uk.ac.le.ember.labpipe.server.sessions.ApiPath
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics
import java.time.LocalDateTime

object RecordService {

    // TODO Add support for multiple records batch upload

    private fun saveRecord(jsonObject: JsonObject): String? {
        val formCode = jsonObject.get("form_code").asString
        val record = Document.parse(Gson().toJson(jsonObject))
        try {
            val collection = Runtime.mongoDatabase
                .getCollection("${Statics.DB_COL_FORM_DATA_PREFIX}$formCode")
            collection.insertOne(record)
        } catch (e: Exception) {
            Runtime.logger.error(e) { "[Form: $formCode] data cannot be saved." }
            return null
        }
        Runtime.logger.info { "[Form: $formCode] data is saved." }
        val id = record.get("_id") as ObjectId
        return id.toString()
    }

    fun routes() {
        println("Add record service routes.")
        Runtime.server.post(ApiPath.RECORD_ADD.value, { ctx ->
            val operator = AuthManager.getUser(ctx)
            operator?.run {
                val jsonParser = JsonParser()
                val record = jsonParser.parse(ctx.body()).asJsonObject
                val formCode = record.get("form_code").asString
                record.addProperty("uploaded_by", operator.username)
                record.addProperty("created", LocalDateTime.now().toString())
                println(ctx.body())
                val recordId = saveRecord(record)
                if (recordId != null) {
                    Runtime.logger.info { "Record saved [$recordId]" }
                    NotificationUtil.sendNotificationEmail(operator, formCode, record)
                    ctx.status(200)
                } else {
                    ctx.result("Record cannot be saved. Please retry or contact service manager.")
                    ctx.status(500)
                }
            }
        }, SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED))
    }
}