package uk.ac.le.ember.labpipe.server

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.litote.kmongo.Data
import uk.ac.le.ember.labpipe.server.controllers.ConfigController
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.net.URLEncoder

@Data
data class Parameter(var identifier: String) {
    var value: MutableList<Any> = mutableListOf()
}

@Data
data class ClientSettings(var identifier: String, var name: String, var value: MutableSet<String>? = mutableSetOf())

@Data
data class Operator(var email: String) {
    var name: String = ""
    var username: String = email
    var passwordHash: String = ""
    var active: Boolean = false

    var projects: MutableSet<String> = mutableSetOf()
    var notificationGroup: MutableSet<String> = mutableSetOf()

    var roles: MutableSet<String> = mutableSetOf()
}

@Data
data class AccessToken(
    var token: String,
    var keyHash: String
) {
    var roles: MutableSet<String> = mutableSetOf()
}

@Data
data class Location(var identifier: String, var name: String) {
    var type: MutableSet<String> = mutableSetOf()
}

@Data
data class OperatorRole(var identifier: String, var name: String)

@Data
data class ApiAccessRole(var url: String, var roles: MutableSet<String>, var rate: Int = 0)

@Data
data class FormTemplate(var identifier: String, var name: String) {
    var studyIdentifier: String = ""

    var instrumentIdentifier: String = ""

    var template: WizardTemplate =
        WizardTemplate("Default Wizard Template")
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

    fun validateOntologyProperties(): MutableList<String> {
        val result = mutableListOf<String>()
        if (Runtime.config[ConfigController.Companion.LabPipeConfig.BioPortal.api].isNotEmpty()) {
            for (p in template.pages) {
                for (q in p.questions) {
                    if (q.ontology != null && !q.ontology!!.validateWithBioportal()) {
                        result.add("Ontology [${q.ontology!!.acronym}] class [${q.ontology!!.classId}] is invalid.")
                    }
                }
            }
        }
        return result
    }
}

@Data
data class ElectronFileFilter(var name: String) {
    var extensions: MutableSet<String> = mutableSetOf()
}

@Data
data class QuestionTemplate(var key: String, var label: String, var controlType: String) {
    var required: Boolean = true
    var order: Int = 1
    var type: String? = null
    var pattern: String? = null
    var options: String = ""
    var helperText: String = ""
    var target: String? = null
    var multiple: Boolean = false
    var filter: MutableSet<ElectronFileFilter> = mutableSetOf()
    var ontology: OntologyClass? = null
}

@Data
data class OntologyClass(var acronym: String, var classId: String) {
    var name: String = ""

    fun validateWithBioportal(): Boolean {
        val classUrl = URLEncoder.encode("${ONTOLOGY.BIOPORTAL.URL_BASE_CLASS}${acronym}/${classId}", "utf-8")
        val (_, _, result) = "${ONTOLOGY.BIOPORTAL.URL_GET_CLASS}${acronym}/classes/${classUrl}".httpGet()
            .responseString()
        return when (result) {
            is Result.Failure -> {
                false
            }
            is Result.Success -> {
                true
            }
        }
    }
}

@Data
data class WizardPageFormValidProcess(var order: Int, var processType: String, var parameters: MutableList<String>) {
    var newField: String? = null
    var auto: Boolean = false
    var onSave: Boolean = false
    var allowCopy: Boolean = false
}

@Data
data class WizardPageTemplate(var key: String, var title: String) {
    var navTitle: String? = ""
    var requireValidForm: Boolean = true
    var order: Int = 1
    var questions: MutableList<QuestionTemplate> = mutableListOf()
    var formValidProcess: MutableList<WizardPageFormValidProcess> = mutableListOf()
}

@Data
data class WizardTemplate(var title: String) {
    var pages: MutableList<WizardPageTemplate> = mutableListOf()
}

@Data
data class EmailGroup(var identifier: String) {
    var name: String = ""

    var studyIdentifier: String = ""

    var formIdentifier: String = ""

    var admin: MutableSet<String> = mutableSetOf()
    var member: MutableSet<String> = mutableSetOf()
}

@Data
data class FormFileUpload(var identifier: String) {
    var files: MutableSet<String> = mutableSetOf()
}

@Data
data class FormFileChunkUpload(var identifier: String, var chunk: Int, var total: Int, var md5Chunk: String, var md5Total: String, var ext: String) {
    var file: String = ""
    var complete: Boolean = false
}

@Data
data class Message(var message: String)

@Data
data class ResultMessage(var status: Int, var message: Message)

@Data
data class Instrument(var identifier: String, var name: String) {
    var realtime: Boolean = false
    var fileType: MutableSet<String> = mutableSetOf()
}

@Data
data class Collector(var identifier: String, var name: String)

@Data
data class SampleType(var identifier: String, var name: String)

@Data
data class Study(var identifier: String) {
    var name: String = ""
    var config: Any? = null
}

@Data
data class Record(var actionIdentifier: String) {
    var formIdentifier: String = ""
    var studyIdentifier: String = ""
    var instrumentIdentifier: String = ""
    var savedBy: String? = null
    var uploadedBy: String? = null
    var created: String? = null
    var record: Any? = null
}