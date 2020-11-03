package uk.ac.le.ember.labpipe.server

import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.controllers.ReportTemplate
import uk.ac.le.ember.labpipe.server.sessions.Runtime

const val DEFAULT_CONFIG_FILE_NAME = "config.toml"
const val DB_COL_FORM_DATA_PREFIX = "FORM_DATA_"
val DEFAULT_ADMIN_ROLE = OperatorRole(identifier = "admin", name = "Admin")
val DEFAULT_TOKEN_ROLE = OperatorRole(identifier = "token", name = "Token")
val DEFAULT_OPERATOR_ROLE = OperatorRole(identifier = "operator", name = "Operator")
val DEFAULT_CLIENT_SETTING = ClientSettings(identifier = "client_init", name = "Parameter list for client init", value = mutableSetOf("LOCATIONS","OPERATORS","STUDIES","INSTRUMENTS","COLLECTORS"))

object API {
    const val ROOT: String = "/api"

    object GENERAL {
        private const val GR_ROOT = "$ROOT/general"
        const val CONN_PUBLIC = "$GR_ROOT/connect/public"
        const val CONN_AUTH = "$GR_ROOT/connect/auth"
        const val CONN_TOKEN = "$GR_ROOT/connect/token"
    }

    object FORM {
        private const val FT_ROOT = "$ROOT/form/template"
        const val ALL = "$FT_ROOT/all"
        const val FROM_IDENTIFIER = "$FT_ROOT/identifier/:identifier"
        const val FROM_STUDY_INSTRUMENT = "$FT_ROOT/study/:studyIdentifier/instrument/:instrumentIdentifier"
    }

    object RECORD {
        private const val RC_ROOT = "$ROOT/record"
        const val ADD = "$RC_ROOT/add"
    }

    object UPLOAD {
        private const val UP_ROOT = "$ROOT/upload"
        const val FORM_FILE = "$UP_ROOT/file/form"
        const val FORM_FILE_CHUNK = "$UP_ROOT/chunk/form"
    }

    object PARAMETER {
        private const val PM_ROOT = "$ROOT/parameter"
        const val FROM_NAME = "$PM_ROOT/identifier/:identifier"
    }

    object QUERY {
        private const val QR_ROOT = "$ROOT/query"
        const val RECORDS = "$QR_ROOT/record/all"
        const val STUDY_RECORDS = "$QR_ROOT/record/all/:studyIdentifier"
        const val STUDIES = "$QR_ROOT/study/all"
        const val STUDY = "$QR_ROOT/study/one"
        const val INSTRUMENTS = "$QR_ROOT/instrument/all"
        const val INSTRUMENT = "$QR_ROOT/instrument/one"
    }

    object MANAGE {
        private const val MG_ROOT = "$ROOT/manage"

        object CREATE {
            private const val CR_ROOT = "$MG_ROOT/create"
            const val OPERATOR = "$CR_ROOT/operator"
            const val TOKEN = "$CR_ROOT/token"
            const val ROLE = "$CR_ROOT/role"
            const val EMAIL_GROUP = "$CR_ROOT/email-group"
            const val INSTRUMENT = "$CR_ROOT/instrument"
            const val LOCATION = "$CR_ROOT/location"
            const val STUDY = "$CR_ROOT/study"
        }

        object UPDATE {
            private const val UD_ROOT = "$MG_ROOT/update"
            const val PASSWORD = "${UD_ROOT}/password"
        }
    }
}

object MESSAGES {
    const val SERVER_RUNNING: String = "LabPipe Server is running."
    const val UNAUTHORIZED: String = "Unauthorised. Invalid authentication credentials in request."
    const val CONN_PUBLIC_SUCCESS = "Access to public resources authorised."
    const val CONN_AUTH_SUCCESS = "Access to resources authorised with operator credentials."
    const val CONN_TOKEN_SUCCESS = "Access to resources authorised with token."
    const val OPERATOR_ADDED = "Operator added."
    const val TOKEN_ADDED = "Access token added."
    const val ROLE_ADDED = "Role added."
    const val EMAIL_GROUP_ADDED = "Email group added."
    const val INSTRUMENT_ADDED = "Instrument added."
    const val LOCATION_ADDED = "Location added."
    const val STUDY_ADDED = "Study added."
    const val FORM_ADDED = "Form added."
}



object NOTIFICATION {
    object STYLE {
        const val DO_NOT_NOTIFY = "DO_NOT_NOTIFY"
        const val NOTIFY_ALL = "NOTIFY_ALL"
        const val OPERATOR_ONLY = "OPERATOR_ONLY"
        const val ADMIN_ONLY = "ADMIN_ONLY"
        const val MEMBER_ONLY = "MEMBER_ONLY"
    }
}

object EmailTemplates {
    const val CREATE_OPERATOR_TEXT =
        "A LabPipe account has been created for you.\n\n" +
                "Name: %s\n" +
                "Email: %s\n" +
                "Password: %s\n\n " +
                "Please note that your email is your username to use LabPipe Client. " +
                "It is recommended that you change your password as soon as possible."
    const val CREATE_OPERATOR_HTML =
        "<p>A LabPipe account has been created for you.<p>" +
                "<br>" +
                "<p><strong>Name:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Email:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Password:</strong></p>" +
                "<p>%s</p>" +
                "Please note that your email is your username to use LabPipe Client. " +
                "It is recommended that you change your password as soon as possible."
    const val CREATE_TOKEN_TEXT =
        "A LabPipe access token has been created for you.\n\n" +
                "Token: %s\n" +
                "Key: %s"
    const val CREATE_TOKEN_HTML =
        "<p>A LabPipe access token has been created for you.<p>" +
                "<br>" +
                "<p><strong>Token:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Key:</strong></p>" +
                "<p>%s</p>"
    const val CREATE_ROLE_TEXT =
        "A LabPipe role has been created by you.\n\n" +
                "Identifier: %s\n" +
                "Name: %s"
    const val CREATE_ROLE_HTML =
        "<p>A LabPipe role has been created by you.<p>" +
                "<br>" +
                "<p><strong>Identifier:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Name:</strong></p>" +
                "<p>%s</p>"
    const val CREATE_EMAILGROUP_TEXT =
        "A LabPipe email group has been created by you.\n\n" +
                "Identifier: %s\n" +
                "Name: %s\n" +
                "Form: %s"
    const val CREATE_EMAILGROUP_HTML =
        "<p>A LabPipe role has been created by you.<p>" +
                "<br>" +
                "<p><strong>Identifier:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Name:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Form:</strong></p>" +
                "<p>%s</p>"
    const val CREATE_INSTRUMENT_TEXT =
        "A LabPipe instrument has been created by you.\n\n" +
                "Identifier: %s\n" +
                "Name: %s"
    const val CREATE_INSTRUMENT_HTML =
        "<p>A LabPipe instrument has been created by you.<p>" +
                "<br>" +
                "<p><strong>Identifier:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Name:</strong></p>" +
                "<p>%s</p>"
    const val CREATE_LOCATION_TEXT =
        "A LabPipe location has been added by you.\n\n" +
                "Identifier: %s\n" +
                "Name: %s"
    const val CREATE_LOCATION_HTML =
        "<p>A LabPipe location has been added by you.<p>" +
                "<br>" +
                "<p><strong>Identifier:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Name:</strong></p>" +
                "<p>%s</p>"
    const val CREATE_STUDY_TEXT =
        "A LabPipe study has been added by you.\n\n" +
                "Identifier: %s\n" +
                "Name: %s"
    const val CREATE_STUDY_HTML =
        "<p>A LabPipe study has been added by you.<p>" +
                "<br>" +
                "<p><strong>Identifier:</strong></p>" +
                "<p>%s</p>" +
                "<p><strong>Name:</strong></p>" +
                "<p>%s</p>"
    const val CREATE_FORM_TEXT =
        "A LabPipe form template has been added by you.\n\n" +
            "Identifier: %s\n" +
            "Name: %s"
    const val CREATE_Form_HTML =
        "<p>A LabPipe form template has been added by you.<p>" +
            "<br>" +
            "<p><strong>Identifier:</strong></p>" +
            "<p>%s</p>" +
            "<p><strong>Name:</strong></p>" +
            "<p>%s</p>"
}

object MONGO {
    object COL_NAMES {
        const val ACCESS_TOKENS = "ACCESS_TOKENS"
        const val ROLES = "ROLES"
        const val CLIENT_SETTINGS = "CLIENT_SETTINGS"
        const val API_ACCESS_ROLES = "API_ACCESS_ROLES"
        const val OPERATORS = "OPERATORS"
        const val STUDIES = "STUDIES"
        const val FORMS = "FORMS"
        const val REPORT_TEMPLATES = "REPORT_TEMPLATES"
        const val INSTRUMENTS = "INSTRUMENTS"
        const val COLLECTORS = "COLLECTORS"
        const val SAMPLE_TYPES = "SAMPLE_TYPES"
        const val LOCATIONS = "LOCATIONS"
        const val EMAIL_GROUPS = "EMAIL_GROUPS"
        const val UPLOADED = "UPLOADED"
        const val CHUNKED = "CHUNKED"
    }

    object COLLECTIONS {
        val ACCESS_TOKENS = Runtime.mongoDatabase.getCollection<AccessToken>(COL_NAMES.ACCESS_TOKENS)
        val ROLES = Runtime.mongoDatabase.getCollection<OperatorRole>(COL_NAMES.ROLES)
        val CLIENT_SETTINGS = Runtime.mongoDatabase.getCollection<ClientSettings>(COL_NAMES.CLIENT_SETTINGS)
        val API_ACCESS_ROLES = Runtime.mongoDatabase.getCollection<ApiAccessRole>(COL_NAMES.API_ACCESS_ROLES)
        val OPERATORS = Runtime.mongoDatabase.getCollection<Operator>(COL_NAMES.OPERATORS)
        val STUDIES = Runtime.mongoDatabase.getCollection<Study>(COL_NAMES.STUDIES)
        val FORMS = Runtime.mongoDatabase.getCollection<FormTemplate>(COL_NAMES.FORMS)
        val REPORT_TEMPLATES = Runtime.mongoDatabase.getCollection<ReportTemplate>(COL_NAMES.REPORT_TEMPLATES)
        val INSTRUMENTS = Runtime.mongoDatabase.getCollection<Instrument>(COL_NAMES.INSTRUMENTS)
        val COLLECTORS = Runtime.mongoDatabase.getCollection<Collector>(COL_NAMES.COLLECTORS)
        val SAMPLE_TYPES = Runtime.mongoDatabase.getCollection<SampleType>(COL_NAMES.SAMPLE_TYPES)
        val LOCATIONS = Runtime.mongoDatabase.getCollection<Location>(COL_NAMES.LOCATIONS)
        val EMAIL_GROUPS = Runtime.mongoDatabase.getCollection<EmailGroup>(COL_NAMES.EMAIL_GROUPS)
        val UPLOADED = Runtime.mongoDatabase.getCollection<FormFileUpload>(COL_NAMES.UPLOADED)
        val CHUNKED = Runtime.mongoDatabase.getCollection<FormFileChunkUpload>(COL_NAMES.CHUNKED)
    }


}