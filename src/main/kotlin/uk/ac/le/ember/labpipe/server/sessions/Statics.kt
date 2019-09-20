package uk.ac.le.ember.labpipe.server.sessions

class Statics {
    companion object {
        const val DEFAULT_CONFIG_FILE_NAME = "config.ini"
        const val DB_COL_FORM_DATA_PREFIX = "FORM_DATA_"
    }
}

enum class PropertyFields(val value: String) {
    SERVER_PORT("server.port"),

    DB_HOST("database.host"),
    DB_PORT("database.port"),
    DB_NAME("database.name"),
    DB_USER("database.user"),
    DB_PASS("database.pass"),

    EMAIL_HOST("mail.host"),
    EMAIL_PORT("mail.port"),
    EMAIL_USER("mail.user"),
    EMAIL_PASS("mail.pass"),
    EMAIL_NOTIFIER_NAME("mail.notifier.name"),
    EMAIL_NOTIFIER_ADDR("mail.notifier.addr"),

    PATH_CACHE("path.cache"),
}

enum class RequiredMongoDBCollections(val value: String) {
    ACCESS_TOKENS("ACCESS_TOKENS"),
    ROLES("ROLES"),
    CLIENT_SETTINGS("CLIENT_SETTINGS"),
    API_ACCESS_ROLES("API_ACCESS_ROLES"),
    OPERATORS("OPERATORS"),
    STUDIES("STUDIES"),
    FORM_TEMPLATES("FORM_TEMPLATES"),
    REPORT_TEMPLATES("REPORT_TEMPLATES"),
    INSTRUMENTS("INSTRUMENTS"),
    COLLECTORS("COLLECTORS"),
    SAMPLE_TYPES("SAMPLE_TYPES"),
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