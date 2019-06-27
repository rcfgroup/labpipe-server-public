package sessions

class BuiltInValue {
    companion object {
        const val PROPERTIES_FIELD_DB_HOST = "database.host"
        const val PROPERTIES_FIELD_DB_PORT = "database.port"
        const val PROPERTIES_FIELD_DB_NAME = "database.name"
        const val PROPERTIES_FIELD_DB_USER = "database.user"
        const val PROPERTIES_FIELD_DB_PASS = "database.pass"

        const val PROPERTIES_FIELD_EMAIL_HOST = "mail.host"
        const val PROPERTIES_FIELD_EMAIL_PORT = "mail.port"
        const val PROPERTIES_FIELD_EMAIL_USER = "mail.user"
        const val PROPERTIES_FIELD_EMAIL_PASS = "mail.pass"

        const val PROPERTIES_FIELD_PATH_CACHE = "path.cache"

        const val DEFAULT_CONFIG_FILE_NAME = "config.ini"

        const val DB_MONGO_COL_ACCESS_TOKEN = "ACCESS_TOKENS"
        const val DB_MONGO_COL_ROLE = "ROLES"
        const val DB_MONGO_COL_CLIENT_SETTING = "CLIENT_SETTINGS"
        const val DB_MONGO_COL_API_ACCESS_ROLES  = "API_ACCESS_ROLES"
        const val DB_MONGO_COL_OPERATORS = "OPERATORS"
        const val DB_MONGO_COL_STUDIES = "STUDIES"
        const val DB_MONGO_COL_FORM_TEMPLATES = "FORM_TEMPLATES"
        const val DB_MONGO_COL_REPORT_TEMPLATES = "REPORT_TEMPLATES"
        const val DB_MONGO_COL_INSTRUMENTS = "INSTRUMENTS"
        const val DB_MONGO_COL_LOCATIONS = "LOCATIONS"
        const val DB_MONGO_COL_EMAIL_GROUPS = "EMAIL_GROUPS"

        const val DB_MONGO_COL_FORM_DATA_PREFIX = "FORM_DATA_"
    }
}