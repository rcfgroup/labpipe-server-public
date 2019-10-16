package uk.ac.le.ember.labpipe.server

import com.mongodb.client.model.Filters.eq
import io.javalin.core.security.Role
import io.javalin.http.Context
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.mindrot.jbcrypt.BCrypt
import uk.ac.le.ember.labpipe.server.data.AccessToken
import uk.ac.le.ember.labpipe.server.data.ApiRoleAssign
import uk.ac.le.ember.labpipe.server.data.Message
import uk.ac.le.ember.labpipe.server.data.Operator
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
                    .json(Message(Constants.MESSAGES.UNAUTHORIZED))
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
        val col = Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
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
        val colOperator = Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
        val operator: Operator? = colOperator.findOne(eq("username", basicAuthCredentials.username))
        if (operator == null) {
            print("No operator auth")
            val colToken =
                Runtime.mongoDatabase.getCollection<AccessToken>(Constants.MONGO.REQUIRED_COLLECTIONS.ACCESS_TOKENS)
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

    fun getApiRoles(url: String): List<String> {
        val apiRole: ApiRoleAssign? =
            Runtime.mongoDatabase.getCollection<ApiRoleAssign>(Constants.MONGO.REQUIRED_COLLECTIONS.API_ACCESS_ROLES)
                .findOne(eq("url", url))
        return apiRole?.roles ?: listOf()
    }
}