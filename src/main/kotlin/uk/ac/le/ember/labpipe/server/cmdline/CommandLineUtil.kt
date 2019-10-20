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
    val configFile = File(Constants.DEFAULT_CONFIG_FILE_NAME)
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
    val configFile = File(Constants.DEFAULT_CONFIG_FILE_NAME)
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
    val configFile = File(Constants.DEFAULT_CONFIG_FILE_NAME)
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
    val configFile = File(Constants.DEFAULT_CONFIG_FILE_NAME)
    val configs = Configurations()
    if (!configFile.exists()) {
        updateConfig(key = Constants.CONFIGS.SERVER_PORT, value = 4567)
        updateConfig(key = Constants.CONFIGS.DB_HOST, value = "localhost")
        updateConfig(key = Constants.CONFIGS.DB_PORT, value = 27017)
        updateConfig(key = Constants.CONFIGS.DB_NAME, value = "labpipe-dev")

        updateConfig(key = Constants.CONFIGS.MAIL_HOST, value = "localhost")
        updateConfig(key = Constants.CONFIGS.MAIL_PORT, value = 25)
        updateConfig(key = Constants.CONFIGS.MAIL_NAME, value = "LabPipe Notification")
        updateConfig(key = Constants.CONFIGS.MAIL_ADDR, value = "no-reply@labpipe.org")

        val defaultCacheDir = Paths.get(System.getProperty("user.home"), "labpipe").toString()
        updateConfig(key = Constants.CONFIGS.PATH_CACHE, value = defaultCacheDir)

        val defaultUploadedDir = Paths.get(System.getProperty("user.home"), "labpipe", "uploaded").toString()
        updateConfig(key = Constants.CONFIGS.PATH_UPLOADED, value = defaultUploadedDir)

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
        Runtime.config = LPConfig(
            serverPort = if (properties.containsKey(Constants.CONFIGS.SERVER_PORT)) properties.getInt(Constants.CONFIGS.SERVER_PORT)
            else 4567
        )
        Runtime.config.cachePath =
            if (properties.containsKey(Constants.CONFIGS.PATH_CACHE)) properties.getString(Constants.CONFIGS.PATH_CACHE)
            else Paths.get(System.getProperty("user.home"), "labpipe").toString()
        Runtime.config.uploadedPath =
            if (properties.containsKey(Constants.CONFIGS.PATH_UPLOADED)) properties.getString(Constants.CONFIGS.PATH_UPLOADED)
            else Paths.get(System.getProperty("user.home"), "labpipe", "uploaded").toString()
        Runtime.config.dbHost =
            if (properties.containsKey(Constants.CONFIGS.DB_HOST)) properties.getString(Constants.CONFIGS.DB_HOST)
            else "localhost"
        Runtime.config.dbPort =
            if (properties.containsKey(Constants.CONFIGS.DB_PORT)) properties.getInt(Constants.CONFIGS.DB_PORT)
            else 27017
        Runtime.config.dbName =
            if (properties.containsKey(Constants.CONFIGS.DB_NAME)) properties.getString(Constants.CONFIGS.DB_NAME)
            else "labpipe"
        Runtime.config.dbUser =
            if (properties.containsKey(Constants.CONFIGS.DB_USER)) properties.getString(Constants.CONFIGS.DB_USER)
            else null
        Runtime.config.dbPass =
            if (properties.containsKey(Constants.CONFIGS.DB_PASS)) properties.getString(Constants.CONFIGS.DB_PASS)
            else null
        Runtime.config.emailHost =
            if (properties.containsKey(Constants.CONFIGS.MAIL_HOST)) properties.getString(Constants.CONFIGS.MAIL_HOST)
            else "localhost"
        Runtime.config.emailPort =
            if (properties.containsKey(Constants.CONFIGS.MAIL_PORT)) properties.getInt(Constants.CONFIGS.MAIL_PORT)
            else 25
        Runtime.config.emailUser =
            if (properties.containsKey(Constants.CONFIGS.MAIL_USER)) properties.getString(Constants.CONFIGS.MAIL_USER)
            else null
        Runtime.config.emailPass =
            if (properties.containsKey(Constants.CONFIGS.MAIL_PASS)) properties.getString(Constants.CONFIGS.MAIL_PASS)
            else null
        Runtime.config.notificationEmailName =
            if (properties.containsKey(Constants.CONFIGS.MAIL_NAME)) properties.getString(Constants.CONFIGS.MAIL_NAME)
            else "LabPipe Notification"
        Runtime.config.notificationEmailAddress =
            if (properties.containsKey(Constants.CONFIGS.MAIL_ADDR)) properties.getString(Constants.CONFIGS.MAIL_ADDR)
            else "no-reply@labpipe.org"
    }
}

fun startServer() {
    Runtime.server = Javalin.create()
    AuthManager.setManager()
    generalRoutes()
    parameterRoutes()
    recordRoutes()
    formRoutes()
    queryRoutes()
    manageRoutes()
    uploadRoutes()
    Runtime.server.start(Runtime.config.serverPort)
    echo("Server running at " + Runtime.config.serverPort)
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
        updateConfig(key = Constants.CONFIGS.SERVER_PORT, value = port)
        updateConfig(key = Constants.CONFIGS.PATH_CACHE, value = cache)
        updateConfig(key = Constants.CONFIGS.PATH_UPLOADED, value = uploaded)
    }
}

class Database : CliktCommand(name = "db", help = "Configure database server") {
    private val host by option("--host", help = "database host")
    private val port by option("--port", help = "database port").int()
    private val name by option("--name", help = "database name")
    private val user by option("--user", help = "database user")
    private val pswd by option("--pass", help = "database password")

    override fun run() {
        updateConfig(key = Constants.CONFIGS.DB_HOST, value = host)
        updateConfig(key = Constants.CONFIGS.DB_PORT, value = port)
        updateConfig(key = Constants.CONFIGS.DB_NAME, value = name)
        updateConfig(key = Constants.CONFIGS.DB_USER, value = user)
        updateConfig(key = Constants.CONFIGS.DB_PASS, value = pswd)
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
        updateConfig(key = Constants.CONFIGS.MAIL_HOST, value = host)
        updateConfig(key = Constants.CONFIGS.MAIL_PORT, value = port)
        updateConfig(key = Constants.CONFIGS.MAIL_USER, value = user)
        updateConfig(key = Constants.CONFIGS.MAIL_PASS, value = pswd)
        updateConfig(
            key = Constants.CONFIGS.MAIL_NAME,
            value = notifierName
        )
        updateConfig(
            key = Constants.CONFIGS.MAIL_ADDR,
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
            if (MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq Constants.DEFAULT_TOKEN_ROLE.identifier) == null) {
                MONGO.COLLECTIONS.ROLES.insertOne(Constants.DEFAULT_TOKEN_ROLE)
            }
        }
        runBlocking {
            if (MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq Constants.DEFAULT_OPERATOR_ROLE.identifier) == null) {
                MONGO.COLLECTIONS.ROLES.insertOne(Constants.DEFAULT_OPERATOR_ROLE)
            }
        }
        runBlocking {
            if (MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq Constants.DEFAULT_ADMIN_ROLE.identifier) == null) {
                MONGO.COLLECTIONS.ROLES.insertOne(Constants.DEFAULT_ADMIN_ROLE)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.GENERAL.CONN_AUTH, roles = mutableSetOf(Constants.DEFAULT_OPERATOR_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.GENERAL.CONN_TOKEN, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.PARAMETER.FROM_NAME, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.FORM.ALL, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.FORM.FROM_IDENTIFIER, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.FORM.FROM_STUDY_INSTRUMENT, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.CREATE.EMAIL_GROUP, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.CREATE.INSTRUMENT, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.CREATE.LOCATION, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.CREATE.OPERATOR, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.UPDATE.PASSWORD, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.CREATE.ROLE, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.CREATE.STUDY, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.MANAGE.CREATE.TOKEN, roles = mutableSetOf(Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.QUERY.INSTRUMENT, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.QUERY.INSTRUMENTS, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.QUERY.RECORDS, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.QUERY.STUDY_RECORDS, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.QUERY.STUDY, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.QUERY.STUDIES, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.RECORD.ADD, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
        runBlocking {
            val record = ApiAccessRole(url = Constants.API.UPLOAD.FORM_FILE, roles = mutableSetOf(Constants.DEFAULT_TOKEN_ROLE.identifier, Constants.DEFAULT_OPERATOR_ROLE.identifier, Constants.DEFAULT_ADMIN_ROLE.identifier))
            if (MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq record.url) == null) {
                MONGO.COLLECTIONS.API_ACCESS_ROLES.insertOne(record)
            }
        }
    }
}