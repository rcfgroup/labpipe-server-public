package uk.ac.le.ember.labpipe.server.sessions

class Statics {
    companion object {
        const val PROPS_FIELD_SERVER_PORT = "server.port"

        const val PROPS_FIELD_DB_HOST = "database.host"
        const val PROPS_FIELD_DB_PORT = "database.port"
        const val PROPS_FIELD_DB_NAME = "database.name"
        const val PROPS_FIELD_DB_USER = "database.user"
        const val PROPS_FIELD_DB_PASS = "database.pass"

        const val PROPS_FIELD_EMAIL_HOST = "mail.host"
        const val PROPS_FIELD_EMAIL_PORT = "mail.port"
        const val PROPS_FIELD_EMAIL_USER = "mail.user"
        const val PROPS_FIELD_EMAIL_PASS = "mail.pass"
        const val PROPS_FIELD_EMAIL_NOTIFICATION_NAME = "mail.notifier.name"
        const val PROPS_FIELD_EMAIL_NOTIFICATION_ADDR = "mail.notifier.addr"

        const val PROPS_FIELD_PATH_CACHE = "path.cache"

        const val DEFAULT_CONFIG_FILE_NAME = "config.ini"


        const val DB_COL_FORM_DATA_PREFIX = "FORM_DATA_"


        const val API_PATH_ROOT = "/api"
        const val API_PATH_FORM_TEMPLATE = "$API_PATH_ROOT/form/template/:formCode"
        const val API_PATH_GENERAL_SERVICE = "$API_PATH_ROOT/general"
        const val API_PATH_GENERAL_CONNECT_PUBLIC = "$API_PATH_GENERAL_SERVICE/connect"
        const val API_PATH_GENERAL_CONNECT_AUTH = "$API_PATH_GENERAL_SERVICE/connect-with-auth"
        const val API_PATH_GENERAL_CONNECT_TOKEN = "$API_PATH_GENERAL_SERVICE/connect-with-token"
        const val API_PATH_RECORD_SERVICE = "$API_PATH_ROOT/record"
        const val API_PATH_RECORD_ADD = "$API_PATH_RECORD_SERVICE/add"
        const val API_PATH_PARAM_SERVICE = "$API_PATH_ROOT/parameter"
        const val API_PATH_PARAM_WITH_NAME = "$API_PATH_PARAM_SERVICE/name/:paramName"
    }
}

enum class RequiredMongoDBCollections(val value: String) {
    ACCESS_TOKEN("ACCESS_TOKENS"),
    ROLE("ROLES"),
    CLIENT_SETTING("CLIENT_SETTINGS"),
    API_ACCESS_ROLES("API_ACCESS_ROLES"),
    OPERATORS("OPERATORS"),
    STUDIES("STUDIES"),
    FORM_TEMPLATES("FORM_TEMPLATES"),
    REPORT_TEMPLATES("REPORT_TEMPLATES"),
    INSTRUMENTS("INSTRUMENTS"),
    LOCATIONS("LOCATIONS"),
    EMAIL_GROUPS("EMAIL_GROUPS")
}

enum class NotificationStyle(val value: String) {
    DO_NOT_NOTIFY("DO_NOT_NOTIFY"),
    NOTIFY_ALL("NOTIFY_ALL"),
    OPERATOR_ONLY("OPERATOR_ONLY"),
    ADMIN_ONLY("ADMIN_ONLY"),
    MEMBER_ONLY("MEMBER_ONLY")
}