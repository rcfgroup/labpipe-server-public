package uk.ac.le.ember.labpipe.server

import com.mongodb.client.model.Filters.eq
import io.javalin.core.security.Role
import io.javalin.http.Context
import mu.KotlinLogging
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.mindrot.jbcrypt.BCrypt
import uk.ac.le.ember.labpipe.server.sessions.Runtime

private val logger = KotlinLogging.logger {}

object AuthManager {
    fun setManager() {
        Runtime.server.config.accessManager { handler, ctx, permittedRoles ->
            val userRole = getUserRole(ctx)
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(eq("url", ctx.matchedPath())) == null || permittedRoles.contains(userRole)) {
                handler.handle(ctx)
            } else {
                ctx.status(401)
                    .json(Message(MESSAGES.UNAUTHORIZED))
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

    private fun getUserRole(ctx: Context): Role {
        try {
            ctx.basicAuthCredentials()
        } catch (e: IllegalArgumentException) {
            return ApiRole.PUBLIC
        }
        val basicAuthCredentials = ctx.basicAuthCredentials()
        val colOperator = Runtime.mongoDatabase.getCollection<Operator>(MONGO.COL_NAMES.OPERATORS)
        val operator: Operator? = colOperator.findOne(eq("username", basicAuthCredentials.username))
        if (operator == null) {
            val colToken =
                Runtime.mongoDatabase.getCollection<AccessToken>(MONGO.COL_NAMES.ACCESS_TOKENS)
            val accessToken: AccessToken? = colToken.findOne(eq("token", basicAuthCredentials.username))
            return if (accessToken == null) {
                ApiRole.PUBLIC
            } else {
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

    private fun getApiRoles(url: String): Set<String> {
        val apiAccessRole: ApiAccessRole? =
            Runtime.mongoDatabase.getCollection<ApiAccessRole>(MONGO.COL_NAMES.API_ACCESS_ROLES)
                .findOne(eq("url", url))
        return apiAccessRole?.roles ?: setOf()
    }
}