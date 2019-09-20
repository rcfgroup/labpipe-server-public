package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.*
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object QueryService {

    private fun all(): List<Any> {
        val colNames = Runtime.mongoDatabase.listCollectionNames()
        val colNameList = colNames.toMutableList().filter { it.startsWith("FORM_DATA_") }
        val records = colNameList.map { Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList() }.flatten()
        return records
    }

    private fun all(study: String?): List<Any> {
        Runtime.logger.info { "Filtering for study: $study" }
        return when(study) {
            null -> {
                val colNames = Runtime.mongoDatabase.listCollectionNames()
                val colNameList = colNames.toMutableList().filter { it.startsWith("FORM_DATA_") }
                colNameList.map { Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList() }.flatten()
            }
            else -> {
                val colNames = Runtime.mongoDatabase.getCollection(RequiredMongoDBCollections.FORM_TEMPLATES.value).aggregate<FormTemplate>(project(excludeId())).toMutableList().filter { it.studyCode.equals(study, true) }.map { "FORM_DATA_${it.code}" }
                colNames.map { Runtime.mongoDatabase.getCollection(it).aggregate<Any>(project(excludeId())).toMutableList() }.flatten()
            }
        }
    }

    fun routes() {
        println("Add query service routes.")
        Runtime.server.get(
            Constants.API.QUERY.ALL, { ctx -> ctx.json(all()) },
            SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED, AuthManager.ApiRole.PUBLIC)
        )
        Runtime.server.get(
            Constants.API.QUERY.ALL_BY_STUDY, { ctx -> ctx.json(all(ctx.pathParam("studyCode"))) },
            SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED, AuthManager.ApiRole.PUBLIC)
        )
    }
}