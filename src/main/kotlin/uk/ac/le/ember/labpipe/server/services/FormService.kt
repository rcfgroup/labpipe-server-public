package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics

object FormService {
    fun routes() {
        println("Add form service routes")
        Runtime.server.get(
            Statics.API_PATH_FORM_TEMPLATE, { ctx -> getFormTemplate(ctx.pathParam("formCode"))?.let { ctx.json(it) } },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }


    fun getFormTemplate(formCode: String): FormTemplate? {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>(RequiredMongoDBCollections.FORM_TEMPLATES.value)
        return col.findOne(FormTemplate::code eq formCode)
    }
}