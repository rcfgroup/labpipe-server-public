package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.javalin.Javalin
import kotlinx.coroutines.runBlocking
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import uk.ac.le.ember.labpipe.server.*
import uk.ac.le.ember.labpipe.server.db.DatabaseUtil
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.services.*
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.io.File
import java.nio.file.Paths


fun updateConfig(key: String, value: String?) {
    val configFile = File(DEFAULT_CONFIG_FILE_NAME)
    configFile.createNewFile()
    val configs = Configurations()
    try {
        val builder = configs.propertiesBuilder(configFile)
        val config = builder.configuration
        if (value != null) {
            config.setProperty(key, value)
            builder.save()
        }
    } catch (cex: ConfigurationException) {
        cex.printStackTrace()
    }
}

fun updateConfig(key: String, value: Int?) {
    val configFile = File(DEFAULT_CONFIG_FILE_NAME)
    configFile.createNewFile()
    val configs = Configurations()
    try {
        val builder = configs.propertiesBuilder(configFile)
        val config = builder.configuration
        if (value != null) {
            config.setProperty(key, value)
            builder.save()
        }
    } catch (cex: ConfigurationException) {
        cex.printStackTrace()
    }
}

fun updateConfig(key: String, value: Boolean) {
    val configFile = File(DEFAULT_CONFIG_FILE_NAME)
    configFile.createNewFile()
    val configs = Configurations()
    try {
        val builder = configs.propertiesBuilder(configFile)
        val config = builder.configuration
        config.setProperty(key, value)
        builder.save()
    } catch (cex: ConfigurationException) {
        cex.printStackTrace()
    }
}

fun readConfig(): PropertiesConfiguration? {
    val configFile = File(DEFAULT_CONFIG_FILE_NAME)
    val configs = Configurations()
    if (!configFile.exists()) {
        updateConfig(key = CONFIGS.SERVER_PORT, value = 4567)
        updateConfig(key = CONFIGS.DB_HOST, value = "localhost")
        updateConfig(key = CONFIGS.DB_PORT, value = 27017)
        updateConfig(key = CONFIGS.DB_NAME, value = "labpipe-dev")

        updateConfig(key = CONFIGS.MAIL_HOST, value = "localhost")
        updateConfig(key = CONFIGS.MAIL_PORT, value = 25)
        updateConfig(key = CONFIGS.MAIL_NAME, value = "LabPipe Notification")
        updateConfig(key = CONFIGS.MAIL_ADDR, value = "no-reply@labpipe.org")

        val defaultCacheDir = Paths.get(System.getProperty("user.home"), "labpipe").toString()
        updateConfig(key = CONFIGS.PATH_CACHE, value = defaultCacheDir)

        val defaultUploadedDir = Paths.get(System.getProperty("user.home"), "labpipe", "uploaded").toString()
        updateConfig(key = CONFIGS.PATH_UPLOADED, value = defaultUploadedDir)

        echo("Using config file: [${configFile.absolutePath}]")
        echo("Default settings:")
        echo("------ Server ------")
        echo("[PORT]: 4567")
        echo("------ Database ------")
        echo("[HOST]: localhost")
        echo("[PORT]: 27017")
        echo("[NAME]: labpipe-dev")
        echo("------ Email Server ------")
        echo("[HOST]: localhost")
        echo("[PORT]: 25")
        echo("[NOTIFY FROM]: LabPipe Notification <no-reply@labpipe.org>")
        echo("------ Directory ------")
        echo("[CACHE]: $defaultCacheDir")
        echo("[UPLOADED]: $defaultUploadedDir")
    }
    return try {
        configs.properties(configFile)
    } catch (cex: ConfigurationException) {
        cex.printStackTrace()
        null
    }
}

fun importConfig() {
    val properties = readConfig()
    properties?.run {
        Runtime.lpConfig = LPConfig(
            serverPort = if (properties.containsKey(CONFIGS.SERVER_PORT)) properties.getInt(CONFIGS.SERVER_PORT)
            else 4567
        )
        Runtime.lpConfig.cachePath =
            if (properties.containsKey(CONFIGS.PATH_CACHE)) properties.getString(CONFIGS.PATH_CACHE)
            else Paths.get(System.getProperty("user.home"), "labpipe").toString()
        Runtime.lpConfig.uploadedPath =
            if (properties.containsKey(CONFIGS.PATH_UPLOADED)) properties.getString(CONFIGS.PATH_UPLOADED)
            else Paths.get(System.getProperty("user.home"), "labpipe", "uploaded").toString()
        Runtime.lpConfig.dbHost =
            if (properties.containsKey(CONFIGS.DB_HOST)) properties.getString(CONFIGS.DB_HOST)
            else "localhost"
        Runtime.lpConfig.dbPort =
            if (properties.containsKey(CONFIGS.DB_PORT)) properties.getInt(CONFIGS.DB_PORT)
            else 27017
        Runtime.lpConfig.dbName =
            if (properties.containsKey(CONFIGS.DB_NAME)) properties.getString(CONFIGS.DB_NAME)
            else "labpipe"
        Runtime.lpConfig.dbUser =
            if (properties.containsKey(CONFIGS.DB_USER)) properties.getString(CONFIGS.DB_USER)
            else null
        Runtime.lpConfig.dbPass =
            if (properties.containsKey(CONFIGS.DB_PASS)) properties.getString(CONFIGS.DB_PASS)
            else null
        Runtime.lpConfig.emailHost =
            if (properties.containsKey(CONFIGS.MAIL_HOST)) properties.getString(CONFIGS.MAIL_HOST)
            else "localhost"
        Runtime.lpConfig.emailPort =
            if (properties.containsKey(CONFIGS.MAIL_PORT)) properties.getInt(CONFIGS.MAIL_PORT)
            else 25
        Runtime.lpConfig.emailUser =
            if (properties.containsKey(CONFIGS.MAIL_USER)) properties.getString(CONFIGS.MAIL_USER)
            else null
        Runtime.lpConfig.emailPass =
            if (properties.containsKey(CONFIGS.MAIL_PASS)) properties.getString(CONFIGS.MAIL_PASS)
            else null
        Runtime.lpConfig.notificationEmailName =
            if (properties.containsKey(CONFIGS.MAIL_NAME)) properties.getString(CONFIGS.MAIL_NAME)
            else "LabPipe Notification"
        Runtime.lpConfig.notificationEmailAddress =
            if (properties.containsKey(CONFIGS.MAIL_ADDR)) properties.getString(CONFIGS.MAIL_ADDR)
            else "no-reply@labpipe.org"
    }
}

fun startServer() {
    Runtime.server = Javalin.create { config -> config.enableCorsForAllOrigins() }
    AuthManager.setManager()
    generalRoutes()
    parameterRoutes()
    recordRoutes()
    formRoutes()
    queryRoutes()
    manageRoutes()
    uploadRoutes()
    Runtime.server.start(Runtime.lpConfig.serverPort)
    echo("Server running at " + Runtime.lpConfig.serverPort)
}

class LPServerCmdLine :
    CliktCommand(name = "LabPipe Server Commandline Tool", help = "LabPipe Server Commandline Tool") {
    override fun run() {
        echo("LabPipe Server")
    }

}

class Config : CliktCommand(name = "config", help = "LabPipe Configuration") {

    override fun run() {
        echo("LabPipe Configuration")
    }
}

class Server : CliktCommand(name = "server", help = "Configure server") {
    private val port by option("--port", help = "server port").int().default(4567)
    private val cache by option("--cache", help = "cache directory")
    private val uploaded by option("--uploaded", help = "uploaded directory")

    override fun run() {
        updateConfig(key = CONFIGS.SERVER_PORT, value = port)
        updateConfig(key = CONFIGS.PATH_CACHE, value = cache)
        updateConfig(key = CONFIGS.PATH_UPLOADED, value = uploaded)
    }
}

class Database : CliktCommand(name = "db", help = "Configure database server") {
    private val host by option("--host", help = "database host")
    private val port by option("--port", help = "database port").int()
    private val name by option("--name", help = "database name")
    private val user by option("--user", help = "database user")
    private val pswd by option("--pass", help = "database password")

    override fun run() {
        updateConfig(key = CONFIGS.DB_HOST, value = host)
        updateConfig(key = CONFIGS.DB_PORT, value = port)
        updateConfig(key = CONFIGS.DB_NAME, value = name)
        updateConfig(key = CONFIGS.DB_USER, value = user)
        updateConfig(key = CONFIGS.DB_PASS, value = pswd)
    }
}

class Email : CliktCommand(name = "email", help = "Configure email server") {
    private val host by option("--host", help = "email server host")
    private val port by option("--port", help = "email server port").int()
    private val user by option("--user", help = "email server user")
    private val pswd by option("--pass", help = "email server password")
    private val notifierName by option("--notifier-name", help = "notification email sender name")
    private val notifierAddr by option(
        "--notifier-addr",
        help = "notification email sender address"
    )

    override fun run() {
        updateConfig(key = CONFIGS.MAIL_HOST, value = host)
        updateConfig(key = CONFIGS.MAIL_PORT, value = port)
        updateConfig(key = CONFIGS.MAIL_USER, value = user)
        updateConfig(key = CONFIGS.MAIL_PASS, value = pswd)
        updateConfig(
            key = CONFIGS.MAIL_NAME,
            value = notifierName
        )
        updateConfig(
            key = CONFIGS.MAIL_ADDR,
            value = notifierAddr
        )
    }
}

class Check : CliktCommand(name = "check", help = "Check database/email connection") {
    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        EmailUtil.testConnection()
        DatabaseUtil.testConnection()
    }
}

class Run : CliktCommand(name = "run", help = "Run server") {
    private val debugMode by option("--debug", help = "debug mode").flag()
    // TODO enable daemon mode

    override fun run() {
        Runtime.debugMode = debugMode
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        EmailUtil.testConnection()
        DatabaseUtil.testConnection()
        startServer()
    }
}

class Init : CliktCommand(name = "init", help = "Init server") {

    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        DatabaseUtil.testConnection()
        runBlocking {
            if (MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq DEFAULT_TOKEN_ROLE.identifier) == null) {
                MONGO.COLLECTIONS.ROLES.insertOne(DEFAULT_TOKEN_ROLE)
            }
        }
        runBlocking {
            if (MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq DEFAULT_OPERATOR_ROLE.identifier) == null) {
                MONGO.COLLECTIONS.ROLES.insertOne(DEFAULT_OPERATOR_ROLE)
            }
        }
        runBlocking {
            if (MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq DEFAULT_ADMIN_ROLE.identifier) == null) {
                MONGO.COLLECTIONS.ROLES.insertOne(DEFAULT_ADMIN_ROLE)
            }
        }
        runBlocking {
            if (MONGO.COLLECTIONS.CLIENT_SETTINGS.findOne(ClientSettings::identifier eq DEFAULT_CLIENT_SETTING.identifier) == null) {
                MONGO.COLLECTIONS.CLIENT_SETTINGS.insertOne(DEFAULT_CLIENT_SETTING)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.GENERAL.CONN_AUTH, roles = mutableSetOf(DEFAULT_OPERATOR_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.GENERAL.CONN_TOKEN, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.PARAMETER.FROM_NAME, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.FORM.ALL, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.FORM.FROM_IDENTIFIER, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.FORM.FROM_STUDY_INSTRUMENT, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.CREATE.EMAIL_GROUP, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.CREATE.INSTRUMENT, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.CREATE.LOCATION, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.CREATE.OPERATOR, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.UPDATE.PASSWORD, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.CREATE.ROLE, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.CREATE.STUDY, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.MANAGE.CREATE.TOKEN, roles = mutableSetOf(DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.QUERY.INSTRUMENT, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.QUERY.INSTRUMENTS, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.QUERY.RECORDS, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.QUERY.STUDY_RECORDS, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.QUERY.STUDY, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.QUERY.STUDIES, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.RECORD.ADD, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = API.UPLOAD.FORM_FILE, roles = mutableSetOf(DEFAULT_TOKEN_ROLE.identifier, DEFAULT_OPERATOR_ROLE.identifier, DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
    }
}