package services

import auths.AuthManager
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles

object GeneralService {
    fun routes(app: Javalin) {
        println("Add general service routes.")
        app.get("/api/general/connect", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.PUBLIC))
        app.get("/api/general/connect-with-auth", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.AUTHORISED))
        app.get("/api/general/connect-with-token", { ctx -> ctx.result("API server connected successfully.") }, roles(AuthManager.ApiRole.TOKEN_AUTHORISED))
    }
}