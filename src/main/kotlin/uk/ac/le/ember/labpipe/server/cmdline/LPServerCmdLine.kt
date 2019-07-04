package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.data.LPConfig
import uk.ac.le.ember.labpipe.server.db.DatabaseUtil
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.services.*
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics
import java.io.File
import java.nio.file.Paths


fun updateConfig(key: String, value: String?) {
    val configFile = File(Statics.DEFAULT_CONFIG_FILE_NAME)
    configFile.createNewFile()
    val configs = Configurations();
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
    val configFile = File(Statics.DEFAULT_CONFIG_FILE_NAME)
    configFile.createNewFile()
    val configs = Configurations();
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
    val configFile = File(Statics.DEFAULT_CONFIG_FILE_NAME)
    configFile.createNewFile()
    val configs = Configurations();
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
    val configFile = File(Statics.DEFAULT_CONFIG_FILE_NAME)
    val configs = Configurations()
    if (!configFile.exists()) {
        updateConfig(key = Statics.PROPS_FIELD_SERVER_PORT, value = 4567)
        updateConfig(key = Statics.PROPS_FIELD_DB_HOST, value = "localhost")
        updateConfig(key = Statics.PROPS_FIELD_DB_PORT, value = 27017)
        updateConfig(key = Statics.PROPS_FIELD_DB_NAME, value = "labpipe-dev")

        updateConfig(key = Statics.PROPS_FIELD_EMAIL_HOST, value = "localhost")
        updateConfig(key = Statics.PROPS_FIELD_EMAIL_PORT, value = 25)
        updateConfig(key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_NAME, value = "LabPipe Notification")
        updateConfig(key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR, value = "no-reply@labpipe.org")

        val defaultCacheDir = Paths.get(System.getProperty("user.home"), "labpipe").toString()
        updateConfig(key = Statics.PROPS_FIELD_PATH_CACHE, value = defaultCacheDir)

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
        echo("------ Cache Directory ------")
        echo("[CACHE]: $defaultCacheDir")
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
            when {
                properties.containsKey(Statics.PROPS_FIELD_SERVER_PORT) -> properties.getInt(Statics.PROPS_FIELD_SERVER_PORT)
                else -> 4567
            }
        )
        Runtime.config.tempPath = when {
            properties.containsKey(Statics.PROPS_FIELD_PATH_CACHE) -> properties.getString(Statics.PROPS_FIELD_PATH_CACHE)
            else -> Paths.get(System.getProperty("user.home"), "labpipe").toString()
        }
        Runtime.config.dbHost = when {
            properties.containsKey(Statics.PROPS_FIELD_DB_HOST) -> properties.getString(Statics.PROPS_FIELD_DB_HOST)
            else -> "localhost"
        }
        Runtime.config.dbPort = when {
            properties.containsKey(Statics.PROPS_FIELD_DB_PORT) -> properties.getInt(Statics.PROPS_FIELD_DB_PORT)
            else -> 27017
        }
        Runtime.config.dbName = when {
            properties.containsKey(Statics.PROPS_FIELD_DB_NAME) -> properties.getString(Statics.PROPS_FIELD_DB_NAME)
            else -> "labpipe-dev"
        }
        Runtime.config.dbUser = when {
            properties.containsKey(Statics.PROPS_FIELD_DB_USER) -> properties.getString(Statics.PROPS_FIELD_DB_USER)
            else -> null
        }
        Runtime.config.dbPass = when {
            properties.containsKey(Statics.PROPS_FIELD_DB_PASS) -> properties.getString(Statics.PROPS_FIELD_DB_PASS)
            else -> null
        }
        Runtime.config.emailHost = when {
            properties.containsKey(Statics.PROPS_FIELD_EMAIL_HOST) -> properties.getString(Statics.PROPS_FIELD_EMAIL_HOST)
            else -> "localhost"
        }
        Runtime.config.emailPort = when {
            properties.containsKey(Statics.PROPS_FIELD_EMAIL_PORT) -> properties.getInt(Statics.PROPS_FIELD_EMAIL_PORT)
            else -> 25
        }
        Runtime.config.emailUser = when {
            properties.containsKey(Statics.PROPS_FIELD_EMAIL_USER) -> properties.getString(Statics.PROPS_FIELD_EMAIL_USER)
            else -> null
        }
        Runtime.config.emailPass = when {
            properties.containsKey(Statics.PROPS_FIELD_EMAIL_PASS) -> properties.getString(Statics.PROPS_FIELD_EMAIL_PASS)
            else -> null
        }
        Runtime.config.notificationEmailName = when {
            properties.containsKey(Statics.PROPS_FIELD_EMAIL_NOTIFICATION_NAME) -> properties.getString(Statics.PROPS_FIELD_EMAIL_NOTIFICATION_NAME)
            else -> "LabPipe Notification"
        }
        Runtime.config.notificationEmailAddress = when {
            properties.containsKey(Statics.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR) -> properties.getString(Statics.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR)
            else -> "no-reply@labpipe.org"
        }
    }
}

fun startServer() {
    AuthManager.setManager()
    GeneralService.routes()
    ParameterService.routes()
    RecordService.routes()
    FormService.routes()
    DevService.routes()
    Runtime.server.start(Runtime.config.serverPort)
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

    override fun run() {
        updateConfig(key = Statics.PROPS_FIELD_SERVER_PORT, value = port)
        updateConfig(key = Statics.PROPS_FIELD_PATH_CACHE, value = cache)
    }
}

class Database : CliktCommand(name = "db", help = "Configure database server") {
    private val host by option("--host", help = "database host")
    private val port by option("--port", help = "database port").int()
    private val name by option("--name", help = "database name")
    private val user by option("--user", help = "database user")
    private val pswd by option("--pass", help = "database password")

    override fun run() {
        updateConfig(key = Statics.PROPS_FIELD_DB_HOST, value = host)
        updateConfig(key = Statics.PROPS_FIELD_DB_PORT, value = port)
        updateConfig(key = Statics.PROPS_FIELD_DB_NAME, value = name)
        updateConfig(key = Statics.PROPS_FIELD_DB_USER, value = user)
        updateConfig(key = Statics.PROPS_FIELD_DB_PASS, value = pswd)
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
        updateConfig(key = Statics.PROPS_FIELD_EMAIL_HOST, value = host)
        updateConfig(key = Statics.PROPS_FIELD_EMAIL_PORT, value = port)
        updateConfig(key = Statics.PROPS_FIELD_EMAIL_USER, value = user)
        updateConfig(key = Statics.PROPS_FIELD_EMAIL_PASS, value = pswd)
        updateConfig(
            key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_NAME,
            value = notifierName
        )
        updateConfig(
            key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR,
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
    private val debugMode by option("-d", "--debug", help = "debug mode").flag()

    override fun run() {
        Runtime.debugMode = debugMode
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        EmailUtil.testConnection()
        DatabaseUtil.testConnection()
    }
}

class Init : CliktCommand(name = "init", help = "Init server") {

    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        EmailUtil.testConnection()
        DatabaseUtil.testConnection()
        RequiredMongoDBCollections.values().forEach { Runtime.mongoDatabase.createCollection(it.value)}
        val cols = RequiredMongoDBCollections.values().map { it.value }.toMutableList()
        cols.removeAll(Runtime.mongoDatabase.listCollectionNames())
        cols.forEach{ Runtime.mongoDatabase.createCollection(it)}
    }
}