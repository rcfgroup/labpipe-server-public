package uk.ac.le.ember.labpipe.server

object Constants {
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

        object PARAMETER {
            private const val PM_ROOT = "$ROOT/parameter"
            const val FROM_NAME = "$PM_ROOT/name/:paramName"
        }

        object QUERY {
            private const val QR_ROOT = "$ROOT/query"
            const val ALL = "$QR_ROOT/list/all"
            const val ALL_BY_STUDY = "$QR_ROOT/list/all/:studyCode"
        }
    }

    object MESSAGES {
        const val UNAUTHORIZED: String = "Unauthorised. Invalid authentication credentials in request."
        const val CONN_PUBLIC_SUCCESS = "Access to public resources authorised."
        const val CONN_AUTH_SUCCESS = "Access to resources authorised with credentials."
        const val CONN_TOKEN_SUCCESS = "Access to resources authorised with token."
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
    }

    object MONGO {

    }
}