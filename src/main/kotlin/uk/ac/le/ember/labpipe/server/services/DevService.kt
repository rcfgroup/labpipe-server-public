package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object DevService {
    fun routes() {
        println("Loading developer service routes.")
        Runtime.server.get(
            "/api/dev/form-template", { ctx -> ctx.json({}) },
            roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }
}