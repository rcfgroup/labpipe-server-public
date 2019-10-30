package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import org.litote.kmongo.aggregate
import org.litote.kmongo.excludeId
import org.litote.kmongo.project
import uk.ac.le.ember.labpipe.server.API
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.sessions.Runtime

private fun getParameter(paramName: String): List<Any> {
    val col = Runtime.mongoDatabase.getCollection(paramName)
    return col.aggregate<Any>(project(excludeId())).toMutableList()
}

fun parameterRoutes() {
    println("Add parameter service routes.")
    Runtime.server.get(
        API.PARAMETER.FROM_NAME, { ctx -> ctx.json(getParameter(ctx.pathParam("identifier"))) },
        roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
    )
}