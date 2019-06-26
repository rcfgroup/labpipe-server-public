package services

import auths.AuthManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import db.DatabaseConnector
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil
import org.bson.Document
import sessions.InMemoryData
import java.util.*

object RecordService {
    private fun saveRecord(jsonElement: JsonElement): List<String> {
        if (jsonElement.isJsonObject) {
            println("single object in request body")
            return listOf(saveSingleDocument(jsonElement))
        } else if (jsonElement.isJsonArray) {
            println("multiple objects in request body")
            val results = ArrayList<String>()
            for (je in jsonElement.asJsonArray) {
                results.add(saveSingleDocument(je))
            }
            return results
        }
        return emptyList()
    }

    private fun saveSingleDocument(jsonElement: JsonElement): String {
        val jsonObject = jsonElement.asJsonObject
        val formCode = jsonObject.get("form_code").asString
        val record = Document.parse(Gson().toJson(jsonElement))
        try {
            val collection = InMemoryData.mongoDatabase
                .getCollection("UPLOADED_FORM_DATA_$formCode")
            collection.insertOne(record)
        } catch (e: Exception) {
            return "[Form: $formCode] data cannot be saved."
        }

        return "[Form: $formCode] data is saved."
    }

    fun routes() {
        println("Add record service routes.")
        InMemoryData.labPipeServer.post("/api/record/add", { ctx ->
            val operator = AuthManager.getUser(ctx)
            val jsonParser = JsonParser()
            val jsonElement = jsonParser.parse(ctx.body())
            println(ctx.body())

            saveRecord(jsonElement)
        }, SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED))
    }
}