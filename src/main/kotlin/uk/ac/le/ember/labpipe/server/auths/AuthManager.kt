package uk.ac.le.ember.labpipe.server.auths

import com.mongodb.client.model.Filters.eq
import uk.ac.le.ember.labpipe.server.data.AccessToken
import uk.ac.le.ember.labpipe.server.data.ApiRoleAssign
import uk.ac.le.ember.labpipe.server.data.Operator
import io.javalin.core.security.Role
import io.javalin.http.Context
import org.litote.kmongo.*
import org.mindrot.jbcrypt.BCrypt
import uk.ac.le.ember.labpipe.server.sessions.StaticValue
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData

object AuthManager {
    fun setManager() {
        RuntimeData.labPipeServer.config.accessManager { handler, ctx, permittedRoles ->
            val userRole = getUserRole(ctx)
            if (permittedRoles.contains(userRole)) {
                handler.handle(ctx)
            } else {
                ctx.status(401).result("Unauthorized. Please make sure correct authentication credentials are provided in the request.")
            }
        }
    }

    fun getUser(ctx: Context): Operator? {
        val basicAuthCredentials = ctx.basicAuthCredentials() ?: return null
        val col = RuntimeData.mongoDatabase.getCollection<Operator>(StaticValue.DB_MONGO_COL_OPERATORS)
        return col.findOne(eq("username",  basicAuthCredentials.username))
    }

    fun getUserRole(ctx: Context): Role {
        val basicAuthCredentials = ctx.basicAuthCredentials() ?: return ApiRole.PUBLIC
        val colOperator = RuntimeData.mongoDatabase.getCollection<Operator>(StaticValue.DB_MONGO_COL_OPERATORS)
        val operator: Operator? = colOperator.findOne(eq("username",  basicAuthCredentials.username))
        if (operator == null) {
            val colToken = RuntimeData.mongoDatabase.getCollection<AccessToken>(StaticValue.DB_MONGO_COL_ACCESS_TOKEN)
            val accessToken: AccessToken? = colToken.findOne(eq("token", basicAuthCredentials.username))
            return if (accessToken == null) {
                ApiRole.PUBLIC
            } else {
                if (BCrypt.checkpw( basicAuthCredentials.password, accessToken.keyHash)) {
                    val apiRoles: MutableSet<String> = getApiRoles(ctx.matchedPath()).toMutableSet()
                    val tokenRoles: Set<String> = accessToken.roles.toSet()
                    apiRoles.retainAll(tokenRoles)
                    if (apiRoles.size > 0) ApiRole.TOKEN_AUTHORISED else ApiRole.UNAUTHORISED
                } else {
                    ApiRole.UNAUTHORISED
                }
            }
        }else {
            return if (BCrypt.checkpw( basicAuthCredentials.password, operator.passwordHash)) {
                val apiRoles: MutableSet<String> = getApiRoles(ctx.matchedPath()).toMutableSet()
                val operatorRoles: Set<String> = operator.roles.toSet()
                apiRoles.retainAll(operatorRoles)
                if (apiRoles.size > 0) ApiRole.AUTHORISED else ApiRole.UNAUTHORISED
            } else {
                ApiRole.UNAUTHORISED
            }
        }
    }

    enum class ApiRole: Role {
        PUBLIC,
        AUTHORISED,
        TOKEN_AUTHORISED,
        UNAUTHORISED,
    }

    fun getApiRoles(url: String): List<String> {
        val col = RuntimeData.mongoDatabase.getCollection<ApiRoleAssign>(StaticValue.DB_MONGO_COL_API_ACCESS_ROLES)
        val apiRole: ApiRoleAssign? = col.findOne(eq("url",  url))
        return apiRole?.roles ?: listOf()
    }
}