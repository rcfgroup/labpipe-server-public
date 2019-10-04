package uk.ac.le.ember.labpipe.server

object Constants {
    const val DEFAULT_CONFIG_FILE_NAME = "config.ini"
    const val DB_COL_FORM_DATA_PREFIX = "FORM_DATA_"

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
            const val FROM_CODE = "$FT_ROOT/code/:formCode"
            const val FROM_STUDY_INSTRUMENT = "$FT_ROOT/study/:studyCode/instrument/:instrumentCode"
        }

        object RECORD {
            private const val RC_ROOT = "$ROOT/record"
            const val ADD = "$RC_ROOT/add"
        }

        object UPLOAD {
            private const val UP_ROOT = "$ROOT/upload"
            const val FORM_FILE = "$UP_ROOT/file/form"
        }

        object PARAMETER {
            private const val PM_ROOT = "$ROOT/parameter"
            const val FROM_NAME = "$PM_ROOT/name/:paramName"
        }

        object QUERY {
            private const val QR_ROOT = "$ROOT/query"
            const val RECORDS = "$QR_ROOT/record/all"
            const val STUDY_RECORDS = "$QR_ROOT/record/all/:studyCode"
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
            }
        }
    }

    object MESSAGES {
        const val SERVER_RUNNING: String = "LabPipe Server is running."
        const val UNAUTHORIZED: String = "Unauthorised. Invalid authentication credentials in request."
        const val CONN_PUBLIC_SUCCESS = "Access to public resources authorised."
        const val CONN_AUTH_SUCCESS = "Access to resources authorised with operator credentials."
        const val CONN_TOKEN_SUCCESS = "Access to resources authorised with token."
        const val OPERATOR_CREATED = "Operator created. Please check your inbox."
        const val TOKEN_CREATED = "Access token created. Please check your inbox."
    }

    object CONFIGS {
        const val SERVER_PORT = "server.port"

        const val DB_HOST = "database.host"
        const val DB_PORT = "database.port"
        const val DB_NAME = "database.name"
        const val DB_USER = "database.user"
        const val DB_PASS = "database.pass"

        const val MAIL_HOST = "mail.host"
        const val MAIL_PORT = "mail.port"
        const val MAIL_USER = "mail.user"
        const val MAIL_PASS = "mail.pass"
        const val MAIL_NAME = "mail.notifier.name"
        const val MAIL_ADDR = "mail.notifier.addr"

        const val PATH_CACHE = "path.cache"
        const val PATH_UPLOADED = "path.uploaded"
    }

    object MONGO {
        object REQUIRED_COLLECTIONS {
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
        }
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
}