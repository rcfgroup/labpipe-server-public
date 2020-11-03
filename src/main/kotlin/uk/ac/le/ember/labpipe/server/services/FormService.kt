package uk.ac.le.ember.labpipe.server.services

import org.litote.kmongo.aggregate
import org.litote.kmongo.eq
import org.litote.kmongo.excludeId
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.project
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.MONGO

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