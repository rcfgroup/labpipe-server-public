package uk.ac.le.ember.labpipe.server.services

import uk.ac.le.ember.labpipe.server.auths.AuthManager
import io.javalin.core.security.SecurityUtil.roles
import org.litote.kmongo.*
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData

object ParameterService {

    private fun getWithParameterName(paramName: String): List<Any> {
        println("Getting parameter [$paramName]")
        val col = RuntimeData.mongoDatabase.getCollection(paramName)
        return listOf(
            col.aggregate<Any>(project(excludeId()))
        )
    }

    fun routes() {
        println("Add parameter service routes.")
        RuntimeData.labPipeServer.get("/api/parameter/name/:paramName", { ctx -> ctx.json(getWithParameterName(ctx.pathParam("paramName"))) },
            roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
    }
}