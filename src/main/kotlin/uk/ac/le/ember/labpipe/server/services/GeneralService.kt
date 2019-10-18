package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.Message
import uk.ac.le.ember.labpipe.server.sessions.Runtime

fun generalRoutes() {
    println("Add general service routes.")
    Runtime.server.get(
        Constants.API.ROOT,
        { ctx -> ctx.json(Message(Constants.MESSAGES.SERVER_RUNNING)) },
        roles(
            AuthManager.ApiRole.PUBLIC,
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED,
            AuthManager.ApiRole.UNAUTHORISED
        )
    )
    Runtime.server.get(
        Constants.API.GENERAL.CONN_PUBLIC,
        { ctx -> ctx.json(Message(Constants.MESSAGES.CONN_PUBLIC_SUCCESS)) },
        roles(AuthManager.ApiRole.PUBLIC)
    )
    Runtime.server.get(
        Constants.API.GENERAL.CONN_AUTH,
        { ctx -> ctx.json(Message(Constants.MESSAGES.CONN_AUTH_SUCCESS)) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.get(
        Constants.API.GENERAL.CONN_TOKEN,
        { ctx -> ctx.json(Message(Constants.MESSAGES.CONN_TOKEN_SUCCESS)) },
        roles(AuthManager.ApiRole.TOKEN_AUTHORISED)
    )
}