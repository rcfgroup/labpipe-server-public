package sessions

class BuiltInValue {
    companion object {
        val PROPERTIES_FIELD_DB_HOST = "database.host"
        val PROPERTIES_FIELD_DB_PORT = "database.port"
        val PROPERTIES_FIELD_DB_NAME = "database.name"
        val PROPERTIES_FIELD_DB_USER = "database.user"
        val PROPERTIES_FIELD_DB_PASS = "database.pass"

        val PROPERTIES_FIELD_EMAIL_HOST = "mail.host"
        val PROPERTIES_FIELD_EMAIL_PORT = "mail.port"
        val PROPERTIES_FIELD_EMAIL_USER = "mail.user"
        val PROPERTIES_FIELD_EMAIL_PASS = "mail.pass"

        val PROPERTIES_FIELD_PATH_CACHE = "path.cache"

        val DEFAULT_CONFIG_FILE_NAME = "config.ini"
    }
}