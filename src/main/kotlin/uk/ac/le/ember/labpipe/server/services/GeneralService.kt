package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.sessions.ApiPath
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics

object GeneralService {
    fun routes() {
        println("Add general service routes.")
        Runtime.server.get(
            ApiPath.GENERAL_CONNECT_PUBLIC.value,
            { ctx -> ctx.result("API server connected successfully.") },
            roles(AuthManager.ApiRole.PUBLIC)
        )
        Runtime.server.get(
            ApiPath.GENERAL_CONNECT_AUTH.value,
            { ctx -> ctx.result("API server connected successfully with authentication.") },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            ApiPath.GENERAL_CONNECT_TOKEN.value,
            { ctx -> ctx.result("API server connected successfully with token.") },
            roles(AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
    }
}