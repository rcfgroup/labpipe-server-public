package data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

data class Parameter(@JsonProperty("param_name") var name: String) {
    @JsonProperty("param_value")
    var value: List<Any> = mutableListOf()
}

data class ParameterCodeName(var code: String, var name: String)

data class ParamOperator(@JsonProperty("email") var email: String) {
    var name: String = ""
    var username: String = ""

    @JsonProperty("passhash")
    var passwordHash: String = ""


    @JsonProperty("isActive")
    var active: Boolean = false

    var projects: List<String> = mutableListOf()

    @JsonProperty("notification_group")
    var notificationGroup: List<String> = mutableListOf()

    var roles: List<String> = mutableListOf()
}

data class ParamAccessToken(var token: String,
                            @JsonProperty("keyhash") var keyHash: String) {
    var roles: List<String> = mutableListOf()
}

data class ParamLocation(var code: String, var name: String) {
    var type: List<String> = mutableListOf()
}

data class ParamRole(val code: String, val name: String)

data class ParamApiRole(var url: String, var roles: List<String>)

data class ParamFormTemplate(var code: String, var name: String) {
    @JsonProperty("study_code")
    var studyCode: String = ""

    @JsonProperty("instrument_code")
    var instrumentCode: String = ""

    var template: WizardTemplate = WizardTemplate("Default Wizard Template")
    var url: String? = null
}

data class ElectronFileFilter(var name: String) {
    var extensions: List<String> = mutableListOf()
}

data class ParamQuestionTemplate(var key: String, var label: String, var controlType: String) {
    var required: Boolean = true
    var order: Int = 1
    var type: String? = null
    var pattern: String? = null
    var options: String = ""
    var helperText: String = ""
    var target: String? = null
    var multiple: Boolean = false
    var filter: List<ElectronFileFilter> = mutableListOf()
}

data class WizardPageFormValidProcess(var processType: String, var dataField: String) {
    var newField: String? = null
}

data class WizardPageTemplate(var key:String, var title: String) {
    var navTitle: String? = ""
    var requireValidForm: Boolean = true
    var order: Int = 1
    var questions: List<ParamQuestionTemplate> = mutableListOf()
    var formValidProcess: List<WizardPageFormValidProcess> = mutableListOf()
}

data class WizardTemplate(var title: String) {
    var pages: List<WizardPageTemplate> = mutableListOf()
}


