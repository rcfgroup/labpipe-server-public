package uk.ac.le.ember.labpipe.server.services

import com.google.gson.JsonObject
import io.javalin.core.security.SecurityUtil.roles
import j2html.TagCreator.*
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.data.FormTemplate
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