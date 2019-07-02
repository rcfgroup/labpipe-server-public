package uk.ac.le.ember.labpipe.server.services

import uk.ac.le.ember.labpipe.server.auths.AuthManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.javalin.core.security.SecurityUtil
import org.bson.Document
import uk.ac.le.ember.labpipe.server.notification.NotificationUtil
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData
import uk.ac.le.ember.labpipe.server.sessions.StaticValue

object RecordService {

    // TODO Add support for multiple records batch upload

    private fun saveRecord(jsonObject: JsonObject): String {
        val formCode = jsonObject.get("form_code").asString
        val record = Document.parse(Gson().toJson(jsonObject))
        try {
            val collection = RuntimeData.mongoDatabase
                .getCollection("${StaticValue.DB_MONGO_COL_FORM_DATA_PREFIX}$formCode")
            collection.insertOne(record)
        } catch (e: Exception) {
            return "[Form: $formCode] data cannot be saved."
        }

        return "[Form: $formCode] data is saved."
    }

    fun routes() {
        println("Add record service routes.")
        RuntimeData.labPipeServer.post("/api/record/add", { ctx ->
            val operator = AuthManager.getUser(ctx)
            operator?.run {
                val jsonParser = JsonParser()
                val record = jsonParser.parse(ctx.body()).asJsonObject
                val formCode = record.get("form_code").asString
                println(ctx.body())
                saveRecord(record)
                NotificationUtil.sendNotificationEmail(operator, formCode)
                ctx.status(200)
            }
        }, SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED))
    }
}