package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.*
import uk.ac.le.ember.labpipe.server.API
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.sessions.Runtime


fun formRoutes() {
    println("Add form service routes")
    Runtime.server.get(
        API.FORM.ALL,
        { ctx -> ctx.json(listForms()) },
        SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
    )
    Runtime.server.get(
        API.FORM.FROM_IDENTIFIER,
        { ctx -> getForm(ctx.pathParam("identifier"))?.let { ctx.json(it) } },
        SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
    )
    Runtime.server.get(
        API.FORM.FROM_STUDY_INSTRUMENT,
        { ctx -> ctx.json(getForm(ctx.pathParam("studyIdentifier"), ctx.pathParam("instrumentIdentifier"))) },
        SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED)
    )
}

fun listForms(): MutableList<FormTemplate> {
    return MONGO.COLLECTIONS.FORMS.aggregate<FormTemplate>(project(excludeId())).toMutableList()
}


fun getForm(identifier: String): FormTemplate? {
    return MONGO.COLLECTIONS.FORMS.findOne(FormTemplate::identifier eq identifier)
}


fun getForm(studyIdentifier: String, instrumentIdentifier: String): List<FormTemplate> {
    return MONGO.COLLECTIONS.FORMS.find(FormTemplate::studyIdentifier eq studyIdentifier, FormTemplate::instrumentIdentifier eq instrumentIdentifier)
        .toMutableList()
}