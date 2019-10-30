package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import uk.ac.le.ember.labpipe.server.API
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.MESSAGES
import uk.ac.le.ember.labpipe.server.Message
import uk.ac.le.ember.labpipe.server.sessions.Runtime

fun generalRoutes() {
    println("Add general service routes.")
    Runtime.server.get(
        API.ROOT,
        { ctx -> ctx.json(Message(MESSAGES.SERVER_RUNNING)) },
        roles(
            AuthManager.ApiRole.PUBLIC,
            AuthManager.ApiRole.AUTHORISED,
            AuthManager.ApiRole.TOKEN_AUTHORISED,
            AuthManager.ApiRole.UNAUTHORISED
        )
    )
    Runtime.server.get(
        API.GENERAL.CONN_PUBLIC,
        { ctx -> ctx.json(Message(MESSAGES.CONN_PUBLIC_SUCCESS)) },
        roles(AuthManager.ApiRole.PUBLIC)
    )
    Runtime.server.get(
        API.GENERAL.CONN_AUTH,
        { ctx -> ctx.json(Message(MESSAGES.CONN_AUTH_SUCCESS)) },
        roles(AuthManager.ApiRole.AUTHORISED)
    )
    Runtime.server.get(
        API.GENERAL.CONN_TOKEN,
        { ctx -> ctx.json(Message(MESSAGES.CONN_TOKEN_SUCCESS)) },
        roles(AuthManager.ApiRole.TOKEN_AUTHORISED)
    )
}