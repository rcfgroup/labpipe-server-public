package notification

import com.google.gson.JsonElement
import com.mongodb.client.model.Filters.eq
import db.DatabaseConnector

object NotificationUtil {
    fun single(jsonElement: JsonElement) {
        if (jsonElement.isJsonObject) {
            var jsonObject = jsonElement.asJsonObject
            var toUser = jsonObject.get("to").asString
            var message = jsonObject.get("message").asJsonObject
            var operators = DatabaseConnector.database.getCollection("parameters").find(eq("param_name", "operators"))
        }
    }
}