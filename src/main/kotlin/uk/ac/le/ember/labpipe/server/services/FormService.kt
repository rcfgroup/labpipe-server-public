package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.*
import uk.ac.le.ember.labpipe.server.API
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.sessions.Runtime



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