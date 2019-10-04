package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object FormService {
    fun routes() {
        println("Add form service routes")
        Runtime.server.get(
            Constants.API.FORM.FROM_IDENTIFIER,
            { ctx -> getFormTemplate(ctx.pathParam("identifier"))?.let { ctx.json(it) } },
            SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
        Runtime.server.get(
            Constants.API.FORM.FROM_STUDY_INSTRUMENT,
            { ctx -> ctx.json(getFormTemplate(ctx.pathParam("studyIdentifier"), ctx.pathParam("instrumentIdentifier"))) },
            SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
        )
    }


    fun getFormTemplate(identifier: String): FormTemplate? {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>(Constants.MONGO.REQUIRED_COLLECTIONS.FORMS)
        return col.findOne(FormTemplate::identifier eq identifier)
    }


    fun getFormTemplate(studyIdentifier: String, instrumentIdentifier: String): List<FormTemplate> {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>(Constants.MONGO.REQUIRED_COLLECTIONS.FORMS)
        return col.find(FormTemplate::studyIdentifier eq studyIdentifier, FormTemplate::instrumentIdentifier eq instrumentIdentifier)
            .toMutableList()
    }
}