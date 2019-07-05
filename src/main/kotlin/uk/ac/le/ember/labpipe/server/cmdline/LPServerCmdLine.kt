package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.javalin.Javalin
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.data.AccessToken
import uk.ac.le.ember.labpipe.server.data.LPConfig
import uk.ac.le.ember.labpipe.server.db.DatabaseUtil
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.services.*
import uk.ac.le.ember.labpipe.server.sessions.PropertyFields
import uk.ac.le.ember.labpipe.server.sessions.RequiredMongoDBCollections
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics
import java.io.File
import java.nio.file.Paths
import org.litote.kmongo.*
import org.mindrot.jbcrypt.BCrypt
import uk.ac.le.ember.labpipe.server.data.ClientSettings


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
        updateConfig(key = PropertyFields.SERVER_PORT.value, value = 4567)
        updateConfig(key = PropertyFields.DB_HOST.value, value = "localhost")
        updateConfig(key = PropertyFields.DB_PORT.value, value = 27017)
        updateConfig(key = PropertyFields.DB_NAME.value, value = "labpipe-dev")

        updateConfig(key = PropertyFields.EMAIL_HOST.value, value = "localhost")
        updateConfig(key = PropertyFields.EMAIL_PORT.value, value = 25)
        updateConfig(key = PropertyFields.EMAIL_NOTIFIER_NAME.value, value = "LabPipe Notification")
        updateConfig(key = PropertyFields.EMAIL_NOTIFIER_ADDR.value, value = "no-reply@labpipe.org")

        val defaultCacheDir = Paths.get(System.getProperty("user.home"), "labpipe").toString()
        updateConfig(key = PropertyFields.PATH_CACHE.value, value = defaultCacheDir)

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
            serverPort = if (properties.containsKey(PropertyFields.SERVER_PORT.value)) properties.getInt(PropertyFields.SERVER_PORT.value)
            else 4567
        )
        Runtime.config.tempPath =
            if (properties.containsKey(PropertyFields.PATH_CACHE.value)) properties.getString(PropertyFields.PATH_CACHE.value)
            else Paths.get(System.getProperty("user.home"), "labpipe").toString()
        Runtime.config.dbHost =
            if (properties.containsKey(PropertyFields.DB_HOST.value)) properties.getString(PropertyFields.DB_HOST.value)
            else "localhost"
        Runtime.config.dbPort =
            if (properties.containsKey(PropertyFields.DB_PORT.value)) properties.getInt(PropertyFields.DB_PORT.value)
            else 27017
        Runtime.config.dbName =
            if (properties.containsKey(PropertyFields.DB_NAME.value)) properties.getString(PropertyFields.DB_NAME.value)
            else "labpipe"
        Runtime.config.dbUser =
            if (properties.containsKey(PropertyFields.DB_USER.value)) properties.getString(PropertyFields.DB_USER.value)
            else null
        Runtime.config.dbPass =
            if (properties.containsKey(PropertyFields.DB_PASS.value)) properties.getString(PropertyFields.DB_PASS.value)
            else null
        Runtime.config.emailHost =
            if (properties.containsKey(PropertyFields.EMAIL_HOST.value)) properties.getString(PropertyFields.EMAIL_HOST.value)
            else "localhost"
        Runtime.config.emailPort =
            if (properties.containsKey(PropertyFields.EMAIL_PORT.value)) properties.getInt(PropertyFields.EMAIL_PORT.value)
            else 25
        Runtime.config.emailUser =
            if (properties.containsKey(PropertyFields.EMAIL_USER.value)) properties.getString(PropertyFields.EMAIL_USER.value)
            else null
        Runtime.config.emailPass =
            if (properties.containsKey(PropertyFields.EMAIL_PASS.value)) properties.getString(PropertyFields.EMAIL_PASS.value)
            else null
        Runtime.config.notificationEmailName =
            if (properties.containsKey(PropertyFields.EMAIL_NOTIFIER_NAME.value)) properties.getString(PropertyFields.EMAIL_NOTIFIER_NAME.value)
            else "LabPipe Notification"
        Runtime.config.notificationEmailAddress =
            if (properties.containsKey(PropertyFields.EMAIL_NOTIFIER_ADDR.value)) properties.getString(PropertyFields.EMAIL_NOTIFIER_ADDR.value)
            else "no-reply@labpipe.org"
    }
}

fun startServer() {
    Runtime.server = Javalin.create()
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
        updateConfig(key = PropertyFields.SERVER_PORT.value, value = port)
        updateConfig(key = PropertyFields.PATH_CACHE.value, value = cache)
    }
}

class Database : CliktCommand(name = "db", help = "Configure database server") {
    private val host by option("--host", help = "database host")
    private val port by option("--port", help = "database port").int()
    private val name by option("--name", help = "database name")
    private val user by option("--user", help = "database user")
    private val pswd by option("--pass", help = "database password")

    override fun run() {
        updateConfig(key = PropertyFields.DB_HOST.value, value = host)
        updateConfig(key = PropertyFields.DB_PORT.value, value = port)
        updateConfig(key = PropertyFields.DB_NAME.value, value = name)
        updateConfig(key = PropertyFields.DB_USER.value, value = user)
        updateConfig(key = PropertyFields.DB_PASS.value, value = pswd)
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
        updateConfig(key = PropertyFields.EMAIL_HOST.value, value = host)
        updateConfig(key = PropertyFields.EMAIL_PORT.value, value = port)
        updateConfig(key = PropertyFields.EMAIL_USER.value, value = user)
        updateConfig(key = PropertyFields.EMAIL_PASS.value, value = pswd)
        updateConfig(
            key = PropertyFields.EMAIL_NOTIFIER_NAME.value,
            value = notifierName
        )
        updateConfig(
            key = PropertyFields.EMAIL_NOTIFIER_ADDR.value,
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
        startServer()
    }
}

class Init : CliktCommand(name = "init", help = "Init server") {

    override fun run() {
        echo("The init function will be available in next release.")
//        importConfig()
//        DatabaseUtil.connect()
//        EmailUtil.connect()
//        EmailUtil.testConnection()
//        DatabaseUtil.testConnection()
//        val cols = RequiredMongoDBCollections.values().map { it.value }.toMutableList()
//        cols.removeAll(Runtime.mongoDatabase.listCollectionNames())
//        cols.forEach { Runtime.mongoDatabase.createCollection(it) }
//        Runtime.mongoDatabase.getCollection<AccessToken>(RequiredMongoDBCollections.ACCESS_TOKENS.value)
//            .insertOne(AccessToken(token = "token", keyHash = BCrypt.hashpw("key", BCrypt.gensalt())))
//        val clientSettings = ClientSettings(code = "client_init", name = "Parameter list for client init")
//        clientSettings.value = mutableListOf(
//            RequiredMongoDBCollections.LOCATIONS.value,
//            RequiredMongoDBCollections.OPERATORS.value,
//            RequiredMongoDBCollections.STUDIES.value,
//            RequiredMongoDBCollections.INSTRUMENTS.value,
//            RequiredMongoDBCollections.COLLECTORS.value
//        )
//        Runtime.mongoDatabase.getCollection<ClientSettings>(RequiredMongoDBCollections.CLIENT_SETTINGS.value)
//            .insertOne(clientSettings)
    }
}