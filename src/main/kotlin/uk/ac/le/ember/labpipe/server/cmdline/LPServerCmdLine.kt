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
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import uk.ac.le.ember.labpipe.server.sessions.Statics
import java.io.File
import java.nio.file.Paths


fun updateConfig(path: String? = null, key: String, value: String?) {
    val configFilePath = path ?: Statics.DEFAULT_CONFIG_FILE_NAME
    val configFile = File(configFilePath)
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

fun updateConfig(path: String? = null, key: String, value: Int?) {
    val configFilePath = path ?: Statics.DEFAULT_CONFIG_FILE_NAME
    val configFile = File(configFilePath)
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

fun updateConfig(path: String? = null, key: String, value: Boolean) {
    val configFilePath = path ?: Statics.DEFAULT_CONFIG_FILE_NAME
    val configFile = File(configFilePath)
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

fun readConfig(path: String? = null): PropertiesConfiguration? {
    val configFilePath = path ?: Statics.DEFAULT_CONFIG_FILE_NAME
    val configFile = File(configFilePath)
    val configs = Configurations()
    if (!configFile.exists()) {
        updateConfig(path = path, key = Statics.PROPS_FIELD_SERVER_PORT, value = 4567)
        updateConfig(path = path, key = Statics.PROPS_FIELD_DB_HOST, value = "localhost")
        updateConfig(path = path, key = Statics.PROPS_FIELD_DB_PORT, value = 27017)
        updateConfig(path = path, key = Statics.PROPS_FIELD_DB_NAME, value = "labpipe-dev")

        updateConfig(path = path, key = Statics.PROPS_FIELD_EMAIL_HOST, value = "localhost")
        updateConfig(path = path, key = Statics.PROPS_FIELD_EMAIL_PORT, value = 25)
        updateConfig(path = path, key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_NAME, value = "LabPipe Notification")
        updateConfig(path = path, key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR, value = "no-reply@labpipe.org")

        val defaultCacheDir = Paths.get(System.getProperty("user.home"), "labpipe").toString()
        updateConfig(path = path, key = Statics.PROPS_FIELD_PATH_CACHE, value = defaultCacheDir)

        echo("Config file not found at: [$path]")
        echo("Created config file at: [${configFile.absolutePath}]")
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

fun importConfig(configPath: String?) {
    val properties = readConfig(configPath)
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
        Runtime.config.debugMode = when {
            properties.containsKey(Statics.PROPS_FIELD_DEBUG_MODE) -> properties.getBoolean(Statics.PROPS_FIELD_DEBUG_MODE)
            else -> false
        }
    }
}

fun startServer(port: Int) {
    AuthManager.setManager()
    GeneralService.routes()
    ParameterService.routes()
    RecordService.routes()
    FormService.routes()
    DevService.routes()
    Runtime.server.start(port)
}

class LPServerCmdLine : CliktCommand() {

    private val runServer by option("-r", "--run", help = "start server").flag()
    private val runConnectionTest by option(
        "-t",
        "--test-connection",
        help = "test database and email server connection"
    ).flag()
    private val debugMode by option("-d", "--debug", help = "debug mode").flag()

    private val configPath by option("--config", help = "config file path")

    private val serverPort by option("--port", help = "server port").int().default(4567)

    private val dbHost by option("--db-host", help = "database host")
    private val dbPort by option("--db-port", help = "database port").int()
    private val dbName by option("--db-name", help = "database name")
    private val dbUser by option("--db-user", help = "database user")
    private val dbPass by option("--db-pass", help = "database password")

    private val emailHost by option("--email-host", help = "email host")
    private val emailPort by option("--email-port", help = "email port").int()
    private val emailUser by option("--email-user", help = "email user")
    private val emailPass by option("--email-pass", help = "email password")
    private val notificationEmailName by option("--email-notification-name", help = "notification email sender name")
    private val notificationEmailAddress by option(
        "--email-notification-address",
        help = "notification email sender address"
    )

    private val cacheDir by option("--cache-dir", help = "cache directory")

    override fun run() {
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_SERVER_PORT, value = serverPort)

        updateConfig(path = configPath, key = Statics.PROPS_FIELD_DB_HOST, value = dbHost)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_DB_PORT, value = dbPort)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_DB_NAME, value = dbName)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_DB_USER, value = dbUser)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_DB_PASS, value = dbPass)

        updateConfig(path = configPath, key = Statics.PROPS_FIELD_EMAIL_HOST, value = emailHost)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_EMAIL_PORT, value = emailPort)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_EMAIL_USER, value = emailUser)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_EMAIL_PASS, value = emailPass)
        updateConfig(
            path = configPath,
            key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_NAME,
            value = notificationEmailName
        )
        updateConfig(
            path = configPath,
            key = Statics.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR,
            value = notificationEmailAddress
        )

        updateConfig(path = configPath, key = Statics.PROPS_FIELD_PATH_CACHE, value = cacheDir)
        updateConfig(path = configPath, key = Statics.PROPS_FIELD_DEBUG_MODE, value = debugMode)

        if (runConnectionTest) {
            importConfig(configPath)
            DatabaseUtil.connect()
            EmailUtil.connect()
            EmailUtil.testConnection()
            DatabaseUtil.testConnection()
        }

        if (runServer) {
            importConfig(configPath)
            DatabaseUtil.connect()
            EmailUtil.connect()
            EmailUtil.testConnection()
            DatabaseUtil.testConnection()
            startServer(serverPort)
        }
    }

}