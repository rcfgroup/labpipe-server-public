package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object FormService {
    fun routes() {
        println("Add form service routes")
        Runtime.server.get(
            Constants.API.FORM.FROM_CODE,
            { ctx -> getFormTemplate(ctx.pathParam("formCode"))?.let { ctx.json(it) } },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            Constants.API.FORM.FROM_STUDY_INSTRUMENT,
            { ctx -> ctx.json(getFormTemplate(ctx.pathParam("studyCode"), ctx.pathParam("instrumentCode"))) },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }


    fun getFormTemplate(formCode: String): FormTemplate? {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>(RequiredMongoDBCollections.FORMS.value)
        return col.findOne(FormTemplate::code eq formCode)
    }


    fun getFormTemplate(studyCode: String, instrumentCode: String): List<FormTemplate> {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>(RequiredMongoDBCollections.FORMS.value)
        return col.find(FormTemplate::studyCode eq studyCode, FormTemplate::instrumentCode eq instrumentCode)
            .toMutableList()
    }
}