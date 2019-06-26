package services

import auths.AuthManager
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import sessions.InMemoryData

object GeneralService {
    fun routes() {
        println("Add general service routes.")
        InMemoryData.labPipeServer.get("/api/general/connect", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.PUBLIC))
        InMemoryData.labPipeServer.get("/api/general/connect-with-auth", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.AUTHORISED))
        InMemoryData.labPipeServer.get("/api/general/connect-with-token", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.TOKEN_AUTHORISED))
    }
}