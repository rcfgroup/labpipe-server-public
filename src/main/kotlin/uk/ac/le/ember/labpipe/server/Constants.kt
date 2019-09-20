package uk.ac.le.ember.labpipe.server

object Constants {
    object API {
        const val ROOT: String = "/api"

        object GENERAL {
            const val GR_ROOT = "${ROOT}/general"
            const val CONN_PUBLIC = "${GR_ROOT}/connect/public"
            const val CONN_AUTH = "${GR_ROOT}/connect/auth"
            const val CONN_TOKEN = "${GR_ROOT}/connect/token"
        }

        object FORM {
            const val FT_ROOT = "${ROOT}/form/template"
            const val FROM_CODE = "${FT_ROOT}/code/:formCode"
            const val FROM_STUDY_INSTRUMENT = "${FT_ROOT}/study/:studyCode/instrument/:instrumentCode"
        }

        object RECORD {
            const val RC_ROOT = "${ROOT}/record"
            const val ADD = "${RC_ROOT}/add"
        }

        object PARAMETER {
            const val PM_ROOT = "${ROOT}/parameter"
            const val FROM_NAME = "${PM_ROOT}/name/:paramName"
        }

        object QUERY {
            const val QR_ROOT = "${ROOT}/query"
            const val ALL = "${QR_ROOT}/list/all"
            const val ALL_BY_STUDY = "${QR_ROOT}/list/all/:studyCode"
        }
    }

    object MSG {
        const val UNAUTHORIZED: String = "Unauthorised. Invalid authentication credentials in request."
        const val CONN_PUBLIC_SUCCESS = "Access to public resources authorised."
        const val CONN_AUTH_SUCCESS = "Access to resources authorised with credentials."
        const val CONN_TOKEN_SUCCESS = "Access to resources authorised with token."
    }

    object CFG {

    }

    object MGDB {

    }
}