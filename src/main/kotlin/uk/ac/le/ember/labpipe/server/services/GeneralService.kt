package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.sessions.ApiPath
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object GeneralService {
    fun routes() {
        println("Add general service routes.")
        Runtime.server.get(
            Constants.API.GENERAL.CONN_PUBLIC,
            { ctx -> ctx.result(Constants.MSG.CONN_PUBLIC_SUCCESS) },
            roles(AuthManager.ApiRole.PUBLIC)
        )
        Runtime.server.get(
            Constants.API.GENERAL.CONN_AUTH,
            { ctx -> ctx.result(Constants.MSG.CONN_AUTH_SUCCESS) },
            roles(AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            Constants.API.GENERAL.CONN_TOKEN,
            { ctx -> ctx.result(Constants.MSG.CONN_TOKEN_SUCCESS) },
            roles(AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
    }
}