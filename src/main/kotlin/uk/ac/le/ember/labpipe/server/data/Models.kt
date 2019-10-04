package uk.ac.le.ember.labpipe.server.data

import java.nio.file.Paths

data class LPConfig(var serverPort: Int = 4567) {
    var cachePath: String = Paths.get(System.getProperty("user.home"), "labpipe").toString()
    var uploadedPath: String = Paths.get(System.getProperty("user.home"), "labpipe", "uploaded").toString()
    var dbHost: String = "localhost"
    var dbPort: Int = 27017
    var dbName: String = "labpipe-dev"
    var dbUser: String? = null
    var dbPass: String? = null

    var emailHost: String = "localhost"
    var emailPort: Int = 25
    var emailUser: String? = null
    var emailPass: String? = null

    var notificationEmailName: String = "LabPipe Notification"
    var notificationEmailAddress: String = "no-reply@labpipe.org"
}

data class Parameter(var identifier: String) {
    var value: MutableList<Any> = mutableListOf()
}

data class ClientSettings(var identifier: String, var name: String) {
    var value: MutableList<String> = mutableListOf()
}

data class Operator(var email: String) {
    var name: String = ""
    var username: String = ""
    var passwordHash: String = ""
    var active: Boolean = false

    var projects: MutableList<String> = mutableListOf()
    var notificationGroup: MutableList<String> = mutableListOf()

    var roles: MutableList<String> = mutableListOf()
}

data class AccessToken(
    var token: String,
    var keyHash: String
) {
    var roles: MutableList<String> = mutableListOf()
}

data class Location(var identifier: String, var name: String) {
    var type: MutableList<String> = mutableListOf()
}

data class OperatorRole(val identifier: String, val name: String)

data class ApiRoleAssign(var url: String, var roles: MutableList<String>)

data class FormTemplate(var identifier: String, var name: String) {
    var studyIdentifier: String = ""

    var instrumentIdentifier: String = ""

    var template: WizardTemplate = WizardTemplate("Default Wizard Template")
    var url: String? = null

    var notificationStyle: String = "all"

    var notificationSubject: String = ""

    fun getProperty(name: String): String? {
        return when (name) {
            "studyIdentifier" -> studyIdentifier
            "instrumentIdentifier" -> instrumentIdentifier
            "template" -> template.toString()
            "url" -> url
            "notificationStyle" -> notificationStyle
            "notificationSubject" -> notificationSubject
            else -> null
        }
    }
}

data class ElectronFileFilter(var name: String) {
    var extensions: MutableList<String> = mutableListOf()
}

data class QuestionTemplate(var key: String, var label: String, var controlType: String) {
    var required: Boolean = true
    var order: Int = 1
    var type: String? = null
    var pattern: String? = null
    var options: String = ""
    var helperText: String = ""
    var target: String? = null
    var multiple: Boolean = false
    var filter: MutableList<ElectronFileFilter> = mutableListOf()
}

data class WizardPageFormValidProcess(var order: Int, var processType: String, var parameters: MutableList<String>) {
    var newField: String? = null
    var auto: Boolean = false
    var onSave: Boolean = false
    var allowCopy: Boolean = false
}

data class WizardPageTemplate(var key: String, var title: String) {
    var navTitle: String? = ""
    var requireValidForm: Boolean = true
    var order: Int = 1
    var questions: MutableList<QuestionTemplate> = mutableListOf()
    var formValidProcess: MutableList<WizardPageFormValidProcess> = mutableListOf()
}

data class WizardTemplate(var title: String) {
    var pages: MutableList<WizardPageTemplate> = mutableListOf()
}

data class EmailGroup(var identifier: String) {
    var name: String = ""

    var studyIdentifier: String = ""

    var formIdentifier: String = ""

    var admin: MutableList<String> = mutableListOf()
    var member: MutableList<String> = mutableListOf()
}

data class FormFileUpload(var identifier: String) {
    var files: MutableList<String> = mutableListOf()
}

data class Message(var message: String)
