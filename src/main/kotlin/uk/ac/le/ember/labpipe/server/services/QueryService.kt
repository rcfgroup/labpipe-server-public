package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import io.javalin.http.Context
import org.litote.kmongo.aggregate
import org.litote.kmongo.excludeId
import org.litote.kmongo.findOne
import org.litote.kmongo.project
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.Message
import uk.ac.le.ember.labpipe.server.sessions.Runtime



private fun listRecords(): List<Any> {
    val colNames = Runtime.mongoDatabase.listCollectionNames()
    val colNameList = colNames.toMutableList().filter { it.startsWith(Constants.DB_COL_FORM_DATA_PREFIX) }
    val records = colNameList.map {
        Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList()
    }.flatten()
    return records
}

private fun listRecords(study: String?): List<Any> {
    Runtime.logger.info { "Filtering for study: $study" }
    return when (study) {
        null -> {
            val colNames = Runtime.mongoDatabase.listCollectionNames()
            val colNameList = colNames.toMutableList().filter { it.startsWith(Constants.DB_COL_FORM_DATA_PREFIX) }
            colNameList.map {
                Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList()
            }.flatten()
        }
        else -> {
            val colNames = Runtime.mongoDatabase.getCollection(Constants.MONGO.REQUIRED_COLLECTIONS.FORMS)
                .aggregate<FormTemplate>(project(excludeId())).toMutableList()
                .filter { it.studyIdentifier.equals(study, true) }.map { "${Constants.DB_COL_FORM_DATA_PREFIX}${it.identifier}" }
            colNames.map {
                Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList()
            }.flatten()
        }
    }
}

private fun listStudies(): List<Any> {
    return Runtime.mongoDatabase.getCollection(Constants.MONGO.REQUIRED_COLLECTIONS.STUDIES).aggregate<Any>(project(excludeId())).toMutableList()
}

private fun findOneStudy(ctx: Context): Context {
    val identifier = ctx.queryParam("identifier")
    val study = Runtime.mongoDatabase.getCollection(Constants.MONGO.REQUIRED_COLLECTIONS.STUDIES).findOne("{identifier: '${identifier}'}")
    study?.run {
        return ctx.status(200).json(study)
    }
    return ctx.status(500).json(Message("Study not exists with given identifier"))
}

private fun listInstruments(): List<Any> {
    return Runtime.mongoDatabase.getCollection(Constants.MONGO.REQUIRED_COLLECTIONS.INSTRUMENTS).aggregate<Any>(project(excludeId())).toMutableList()
}

private fun findOneInstrument(ctx: Context): Context {
    val identifier = ctx.queryParam("identifier")
    val instrument = Runtime.mongoDatabase.getCollection(Constants.MONGO.REQUIRED_COLLECTIONS.INSTRUMENTS).findOne("{identifier: '${identifier}'}")
    instrument?.run {
        return ctx.status(200).json(instrument)
    }
    return ctx.status(500).json(Message("Instrument not exists with given identifier"))
}

fun queryRoutes() {
    println("Add query service routes.")
    Runtime.server.get(
        Constants.API.QUERY.RECORDS, { ctx -> ctx.json(listRecords()) },
        SecurityUtil.roles(
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED
        )
    )
    Runtime.server.get(
        Constants.API.QUERY.STUDY_RECORDS, { ctx -> ctx.json(listRecords(ctx.pathParam("studyIdentifier"))) },
        SecurityUtil.roles(
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED
        )
    )
    Runtime.server.get(
        Constants.API.QUERY.STUDIES, { ctx -> ctx.json(listStudies()) },
        SecurityUtil.roles(
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED
        )
    )
    Runtime.server.get(
        Constants.API.QUERY.STUDY, { ctx -> findOneStudy(ctx) },
        SecurityUtil.roles(
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED
        )
    )
    Runtime.server.get(
        Constants.API.QUERY.INSTRUMENTS, { ctx -> ctx.json(listInstruments()) },
        SecurityUtil.roles(
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED
        )
    )
    Runtime.server.get(
        Constants.API.QUERY.INSTRUMENT, { ctx -> findOneInstrument(ctx) },
        SecurityUtil.roles(
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED
        )
    )
}