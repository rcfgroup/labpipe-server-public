package uk.ac.le.ember.labpipe.server.services

import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.sessions.StaticValue
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData

object FormService {
    fun routes() {
        println("Add form service routes")
        RuntimeData.labPipeServer.get("/api/form/template/:formCode", { ctx -> getFormTemplate(ctx.pathParam("formCode"))?.let { ctx.json(it) } },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }


    fun getFormTemplate(formCode: String): FormTemplate? {
        val col = RuntimeData.mongoDatabase.getCollection<FormTemplate>(StaticValue.DB_MONGO_COL_FORM_TEMPLATES)
        return col.findOne(FormTemplate::code eq formCode)
    }
}