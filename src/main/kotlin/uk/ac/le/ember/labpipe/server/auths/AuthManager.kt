package uk.ac.le.ember.labpipe.server.auths

import com.mongodb.client.model.Filters.eq
import io.javalin.core.security.Role
import io.javalin.http.Context
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.mindrot.jbcrypt.BCrypt
import uk.ac.le.ember.labpipe.server.data.AccessToken
import uk.ac.le.ember.labpipe.server.data.ApiRoleAssign
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object AuthManager {
    fun setManager() {
        Runtime.server.config.accessManager { handler, ctx, permittedRoles ->
            val userRole = getUserRole(ctx)
            if (permittedRoles.contains(userRole)) {
                handler.handle(ctx)
            } else {
                ctx.status(401)
                    .result("Unauthorized. Please make sure correct authentication credentials are provided in the request.")
            }
        }
    }

    fun getUser(ctx: Context): Operator? {
        val basicAuthCredentials = ctx.basicAuthCredentials() ?: return null
        val col = Runtime.mongoDatabase.getCollection<Operator>(RequiredMongoDBCollections.OPERATORS.value)
        return col.findOne(eq("username", basicAuthCredentials.username))
    }

    fun getUserRole(ctx: Context): Role {
        val basicAuthCredentials = ctx.basicAuthCredentials() ?: return ApiRole.PUBLIC
        val colOperator = Runtime.mongoDatabase.getCollection<Operator>(RequiredMongoDBCollections.OPERATORS.value)
        val operator: Operator? = colOperator.findOne(eq("username", basicAuthCredentials.username))
        if (operator == null) {
            val colToken = Runtime.mongoDatabase.getCollection<AccessToken>(RequiredMongoDBCollections.ACCESS_TOKEN.value)
            val accessToken: AccessToken? = colToken.findOne(eq("token", basicAuthCredentials.username))
            return if (accessToken == null) {
                ApiRole.PUBLIC
            } else {
                if (BCrypt.checkpw(basicAuthCredentials.password, accessToken.keyHash)) {
                    val apiRoles: MutableSet<String> = getApiRoles(ctx.matchedPath()).toMutableSet()
                    val tokenRoles: Set<String> = accessToken.roles.toSet()
                    apiRoles.retainAll(tokenRoles)
                    if (apiRoles.size > 0) ApiRole.TOKEN_AUTHORISED else ApiRole.UNAUTHORISED
                } else {
                    ApiRole.UNAUTHORISED
                }
            }
        } else {
            return if (BCrypt.checkpw(basicAuthCredentials.password, operator.passwordHash)) {
                val apiRoles: MutableSet<String> = getApiRoles(ctx.matchedPath()).toMutableSet()
                val operatorRoles: Set<String> = operator.roles.toSet()
                apiRoles.retainAll(operatorRoles)
                if (apiRoles.size > 0) ApiRole.AUTHORISED else ApiRole.UNAUTHORISED
            } else {
                ApiRole.UNAUTHORISED
            }
        }
    }

    enum class ApiRole : Role {
        PUBLIC,
        AUTHORISED,
        TOKEN_AUTHORISED,
        UNAUTHORISED,
    }

    fun getApiRoles(url: String): List<String> {
        val col = Runtime.mongoDatabase.getCollection<ApiRoleAssign>(RequiredMongoDBCollections.API_ACCESS_ROLES.value)
        val apiRole: ApiRoleAssign? = col.findOne(eq("url", url))
        return apiRole?.roles ?: listOf()
    }
}