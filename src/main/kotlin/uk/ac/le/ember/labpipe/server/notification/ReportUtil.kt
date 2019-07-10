package uk.ac.le.ember.labpipe.server.notification

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.data.Operator

object ReportUtil {
    fun generateHtml(operator: Operator, form: FormTemplate, formData: JsonObject) {

    }

    fun getFormDataAsString(formData: JsonObject, keyString: String): String {
        return if (keyString.contains('.')) {
            val keyArray = keyString.split(".")
            formData.getAsJsonObject(keyArray[0]).get(keyArray[1]).asString
        } else {
            formData.get(keyString).asString
        }
    }

    fun getFormDataAsArray(formData: JsonObject, keyString: String): JsonArray {
        return if (keyString.contains('.')) {
            val keyArray = keyString.split(".")
            formData.getAsJsonObject(keyArray[0]).getAsJsonArray(keyArray[1])
        } else {
            formData.getAsJsonArray(keyString)
        }
    }
}