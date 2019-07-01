package uk.ac.le.ember.labpipe.server.services

import uk.ac.le.ember.labpipe.server.auths.AuthManager
import io.javalin.core.security.SecurityUtil.roles
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData

object GeneralService {
    fun routes() {
        println("Add general service routes.")
        RuntimeData.labPipeServer.get("/api/general/connect", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.PUBLIC))
        RuntimeData.labPipeServer.get("/api/general/connect-with-auth", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.AUTHORISED))
        RuntimeData.labPipeServer.get("/api/general/connect-with-token", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.TOKEN_AUTHORISED))
    }
}