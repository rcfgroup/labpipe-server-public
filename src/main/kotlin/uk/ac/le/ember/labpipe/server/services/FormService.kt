package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.*
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.sessions.ApiPath
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics

object FormService {
    fun routes() {
        println("Add form service routes")
        Runtime.server.get(
            ApiPath.FORM_TEMPLATE_WITH_FORMCODE.value, { ctx -> getFormTemplate(ctx.pathParam("formCode"))?.let { ctx.json(it) } },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            ApiPath.FORM_TEMPLATE_WITH_STUDYCODE_INSTRUMENTCODE.value, { ctx -> ctx.json(getFormTemplate(ctx.pathParam("studyCode"), ctx.pathParam("instrumentCode"))) },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }


    fun getFormTemplate(formCode: String): FormTemplate? {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>(RequiredMongoDBCollections.FORM_TEMPLATES.value)
        return col.findOne(FormTemplate::code eq formCode)
    }


    fun getFormTemplate(studyCode: String, instrumentCode: String): List<FormTemplate> {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>(RequiredMongoDBCollections.FORM_TEMPLATES.value)
        return col.find(FormTemplate::studyCode eq studyCode, FormTemplate::instrumentCode eq instrumentCode).toMutableList()
    }
}