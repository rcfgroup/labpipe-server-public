package services

import auths.AuthManager
import db.DatabaseConnector
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import org.litote.kmongo.*

object ParameterService {

    private fun getWithParameterName(paramName: String): List<Any> {
        println("Getting parameter [$paramName]")
        val col = DatabaseConnector.database.getCollection(paramName)
        return col.aggregate<Any>(
            project(excludeId())
        ).toList()
    }

    fun routes(app: Javalin) {
        println("Add parameter service routes.")
        app.get("/api/parameter/name/:paramName", { ctx -> ctx.json(getWithParameterName(ctx.pathParam("paramName"))) },
            roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
    }
}