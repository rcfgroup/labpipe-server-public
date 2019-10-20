package uk.ac.le.ember.labpipe.server

import com.mongodb.client.model.Filters.eq
import io.javalin.core.security.Role
import io.javalin.http.Context
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.mindrot.jbcrypt.BCrypt
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object AuthManager {
    fun setManager() {
        Runtime.server.config.accessManager { handler, ctx, permittedRoles ->
            println(ctx.path())
            val userRole = getUserRole(ctx)
            if (permittedRoles.contains(userRole)) {
                handler.handle(ctx)
            } else {
                ctx.status(401)
                    .result(Constants.MESSAGES.UNAUTHORIZED)
            }
        }
    }

    fun getUser(ctx: Context): Operator? {
        try {
            ctx.basicAuthCredentials()
        } catch (e: IllegalArgumentException) {
            return null
        }
        val basicAuthCredentials = ctx.basicAuthCredentials()
        val col = Runtime.mongoDatabase.getCollection<Operator>(MONGO.COL_NAMES.OPERATORS)
        return col.findOne(eq("username", basicAuthCredentials.username))
    }

    fun getUserRole(ctx: Context): Role {
        try {
            ctx.basicAuthCredentials()
        } catch (e: IllegalArgumentException) {
            print("No basic auth.")
            return ApiRole.PUBLIC
        }
        val basicAuthCredentials = ctx.basicAuthCredentials()
        val colOperator = Runtime.mongoDatabase.getCollection<Operator>(MONGO.COL_NAMES.OPERATORS)
        val operator: Operator? = colOperator.findOne(eq("username", basicAuthCredentials.username))
        if (operator == null) {
            print("No operator auth")
            val colToken =
                Runtime.mongoDatabase.getCollection<AccessToken>(MONGO.COL_NAMES.ACCESS_TOKENS)
            val accessToken: AccessToken? = colToken.findOne(eq("token", basicAuthCredentials.username))
            return if (accessToken == null) {
                ApiRole.PUBLIC
            } else {
                print(accessToken)
                if (BCrypt.checkpw(basicAuthCredentials.password, accessToken.keyHash)) {
                    val apiRoles: MutableSet<String> = getApiRoles(ctx.matchedPath())
                        .toMutableSet()
                    if (apiRoles.isNullOrEmpty()) {
                        ApiRole.PUBLIC
                    } else {
                        val tokenRoles: Set<String> = accessToken.roles.toSet()
                        apiRoles.retainAll(tokenRoles)
                        if (apiRoles.isNotEmpty()) ApiRole.TOKEN_AUTHORISED else ApiRole.UNAUTHORISED
                    }
                } else ApiRole.UNAUTHORISED
            }
        } else {
            print(operator.name)
            return if (BCrypt.checkpw(basicAuthCredentials.password, operator.passwordHash)) {
                val apiRoles: MutableSet<String> = getApiRoles(ctx.matchedPath())
                    .toMutableSet()
                if (apiRoles.isNullOrEmpty()) {
                    ApiRole.PUBLIC
                } else {
                    val operatorRoles: Set<String> = operator.roles.toSet()
                    apiRoles.retainAll(operatorRoles)
                    if (apiRoles.isNotEmpty()) ApiRole.AUTHORISED else ApiRole.UNAUTHORISED
                }
            } else ApiRole.UNAUTHORISED
        }
    }

    enum class ApiRole : Role {
        PUBLIC,
        AUTHORISED,
        TOKEN_AUTHORISED,
        UNAUTHORISED,
    }

    fun getApiRoles(url: String): Set<String> {
        val apiAccessRole: uk.ac.le.ember.labpipe.server.ApiAccessRole? =
            Runtime.mongoDatabase.getCollection<uk.ac.le.ember.labpipe.server.ApiAccessRole>(MONGO.COL_NAMES.API_ACCESS_ROLES)
                .findOne(eq("url", url))
        return apiAccessRole?.roles ?: setOf()
    }
}