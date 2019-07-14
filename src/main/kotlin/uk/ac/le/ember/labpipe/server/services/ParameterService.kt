package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import org.litote.kmongo.aggregate
import org.litote.kmongo.excludeId
import org.litote.kmongo.project
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.sessions.ApiPath
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object ParameterService {

    private fun getWithParameterName(paramName: String): List<Any> {
        println("Getting parameter [$paramName]")
        val col = Runtime.mongoDatabase.getCollection(paramName)
        return col.aggregate<Any>(project(excludeId())).toMutableList()
    }

    fun routes() {
        println("Add parameter service routes.")
        Runtime.server.get(
            ApiPath.PARAM_WITH_NAME.value, { ctx -> ctx.json(getWithParameterName(ctx.pathParam("paramName"))) },
            roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
    }
}