package uk.ac.le.ember.labpipe.server.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import mu.KotlinLogging
import org.bson.Document
import org.bson.types.ObjectId
import uk.ac.le.ember.labpipe.server.DB_COL_FORM_DATA_PREFIX
import uk.ac.le.ember.labpipe.server.sessions.Runtime

private val logger = KotlinLogging.logger {}

// TODO Add support for multiple records batch upload

fun saveRecord(jsonObject: JsonObject): String? {
    val formIdentifier = jsonObject.get("formIdentifier").asString
    val record = Document.parse(Gson().toJson(jsonObject))
    try {
        val col = Runtime.mongoDatabase
            .getCollection("${DB_COL_FORM_DATA_PREFIX}$formIdentifier")
        col.insertOne(record)
    } catch (e: Exception) {
        logger.error(e) { "[Form: $formIdentifier] data cannot be saved." }
        return null
    }
    logger.info { "[Form: $formIdentifier] data is saved." }
    // TODO post record process plugin here in next release
    val id = record.get("_id") as ObjectId
    return id.toString()
}

// TODO
// fun addRecord(record: Record, operator: Operator? = null): Context {
//    val col = Runtime.mongoDatabase.getCollection<Record>("${DB_COL_FORM_DATA_PREFIX}${record.formIdentifier}")
//    record.created = LocalDateTime.now().toString()
//    record.uploadedBy = operator?.username
//    val current = col.findOne(Record::actionIdentifier eq record.actionIdentifier)
//    current?.run {
//        col.insertOne(record)
//        operator?.run {
//            NotificationUtil.sendNotificationEmail(operator, record)
//        }
//    }
//}
