package uk.ac.le.ember.labpipe.server.services

import io.javalin.http.Context
import mu.KotlinLogging
import org.litote.kmongo.aggregate
import org.litote.kmongo.excludeId
import org.litote.kmongo.findOne
import org.litote.kmongo.project
import uk.ac.le.ember.labpipe.server.DB_COL_FORM_DATA_PREFIX
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.Message
import uk.ac.le.ember.labpipe.server.sessions.Runtime

private val logger = KotlinLogging.logger {}

fun listRecords(): List<Any> {
    val colNames = Runtime.mongoDatabase.listCollectionNames()
    val colNameList = colNames.toMutableList().filter { it.startsWith(DB_COL_FORM_DATA_PREFIX) }
    val records = colNameList.map {
        Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList()
    }.flatten()
    return records
}

fun listRecords(study: String?): List<Any> {
    return when (study) {
        null -> {
            val colNames = Runtime.mongoDatabase.listCollectionNames()
            val colNameList = colNames.toMutableList().filter { it.startsWith(DB_COL_FORM_DATA_PREFIX) }
            colNameList.map {
                Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList()
            }.flatten()
        }
        else -> {
            val colNames = Runtime.mongoDatabase.getCollection(MONGO.COL_NAMES.FORMS)
                .aggregate<FormTemplate>(project(excludeId())).toMutableList()
                .filter { it.studyIdentifier.equals(study, true) }.map { "${DB_COL_FORM_DATA_PREFIX}${it.identifier}" }
            colNames.map {
                Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList()
            }.flatten()
        }
    }
}

fun listStudies(): List<Any> {
    return Runtime.mongoDatabase.getCollection(MONGO.COL_NAMES.STUDIES).aggregate<Any>(project(excludeId())).toMutableList()
}

fun findOneStudy(ctx: Context): Context {
    val identifier = ctx.queryParam("identifier")
    val study = Runtime.mongoDatabase.getCollection(MONGO.COL_NAMES.STUDIES).findOne("{identifier: '${identifier}'}")
    study?.run {
        return ctx.status(200).json(study)
    }
    return ctx.status(500).json(Message("Study not exists with given identifier"))
}

fun listInstruments(): List<Any> {
    return Runtime.mongoDatabase.getCollection(MONGO.COL_NAMES.INSTRUMENTS).aggregate<Any>(project(excludeId())).toMutableList()
}

fun findOneInstrument(ctx: Context): Context {
    val identifier = ctx.queryParam("identifier")
    val instrument = Runtime.mongoDatabase.getCollection(MONGO.COL_NAMES.INSTRUMENTS).findOne("{identifier: '${identifier}'}")
    instrument?.run {
        return ctx.status(200).json(instrument)
    }
    return ctx.status(500).json(Message("Instrument not exists with given identifier"))
}