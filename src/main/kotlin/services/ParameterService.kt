package services

import auths.AuthManager
import db.DatabaseConnector
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import org.litote.kmongo.*
import sessions.InMemoryData

object ParameterService {

    private fun getWithParameterName(paramName: String): List<Any> {
        println("Getting parameter [$paramName]")
        val col = InMemoryData.mongoDatabase.getCollection(paramName)
        return listOf(
            col.aggregate<Any>(project(excludeId()))
        )
    }

    fun routes() {
        println("Add parameter service routes.")
        InMemoryData.labPipeServer.get("/api/parameter/name/:paramName", { ctx -> ctx.json(getWithParameterName(ctx.pathParam("paramName"))) },
            roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
    }
}