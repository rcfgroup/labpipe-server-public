package uk.ac.le.ember.labpipe.server.cmdline

import uk.ac.le.ember.labpipe.server.auths.AuthManager
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import uk.ac.le.ember.labpipe.server.db.DatabaseUtil
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import uk.ac.le.ember.labpipe.server.data.LPConfig
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.services.*
import uk.ac.le.ember.labpipe.server.sessions.StaticValue
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData
import java.io.File
import java.nio.file.Paths

fun updateConfig(path:String? = null, key: String, value: String?) {
    val configFilePath = path ?: StaticValue.DEFAULT_CONFIG_FILE_NAME
    val configFile = File(configFilePath)
    configFile.createNewFile()
    val configs = Configurations();
    try
    {
        val builder = configs.propertiesBuilder(configFile)
        val config = builder.configuration
        if (value != null) {
            config.setProperty(key, value)
            builder.save()
        }
    }
    catch (cex: ConfigurationException)
    {
        cex.printStackTrace()
    }
}

fun updateConfig(path:String? = null, key: String, value: Int?) {
    val configFilePath = path ?: StaticValue.DEFAULT_CONFIG_FILE_NAME
    val configFile = File(configFilePath)
    configFile.createNewFile()
    val configs = Configurations();
    try
    {
        val builder = configs.propertiesBuilder(configFile)
        val config = builder.configuration
        if (value != null) {
            config.setProperty(key, value)
            builder.save()
        }
    }
    catch (cex: ConfigurationException)
    {
        cex.printStackTrace()
    }
}

fun readConfig(path: String? = null): PropertiesConfiguration? {
    val configFilePath = path ?: StaticValue.DEFAULT_CONFIG_FILE_NAME
    val configFile = File(configFilePath)
    val configs = Configurations()
    if (!configFile.exists()) {
        updateConfig(path = path, key = StaticValue.PROPS_FIELD_DB_HOST, value = "localhost")
        updateConfig(path = path, key = StaticValue.PROPS_FIELD_DB_PORT, value = 27017)
        updateConfig(path = path, key = StaticValue.PROPS_FIELD_DB_NAME, value = "labpipe-dev")

        updateConfig(path = path, key = StaticValue.PROPS_FIELD_EMAIL_HOST, value = "localhost")
        updateConfig(path = path, key = StaticValue.PROPS_FIELD_EMAIL_PORT, value = 25)
        updateConfig(path = path, key = StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_NAME, value = "LabPipe Notification")
        updateConfig(path = path, key = StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR, value = "no-reply@labpipe.org")

        val defaultCacheDir = Paths.get(System.getProperty("user.home"), "labpipe").toString()
        updateConfig(path = path, key = StaticValue.PROPS_FIELD_PATH_CACHE, value = defaultCacheDir)

        echo("Config file not found at: [$path]")
        echo("Created config file at: [${configFile.absolutePath}]")
        echo("Default settings:")
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
    return try
    {
        configs.properties(configFile)
    }
    catch (cex: ConfigurationException)
    {
        cex.printStackTrace()
        null
    }
}

fun importConfig(configPath: String?) {
    val properties = readConfig(configPath)
    properties?.run {
        RuntimeData.labPipeConfig = LPConfig(properties.getString(StaticValue.PROPS_FIELD_PATH_CACHE) ?: Paths.get(System.getProperty("user.home"), "labpipe").toString())
        RuntimeData.labPipeConfig.dbHost = when {
            properties.containsKey(StaticValue.PROPS_FIELD_DB_HOST) -> properties.getString(StaticValue.PROPS_FIELD_DB_HOST)
            else -> "localhost"
        }
        RuntimeData.labPipeConfig.dbPort = when {
            properties.containsKey(StaticValue.PROPS_FIELD_DB_PORT) -> properties.getInt(StaticValue.PROPS_FIELD_DB_PORT)
            else -> 27017
        }
        RuntimeData.labPipeConfig.dbName = when {
            properties.containsKey(StaticValue.PROPS_FIELD_DB_NAME) -> properties.getString(StaticValue.PROPS_FIELD_DB_NAME)
            else -> "labpipe-dev"
        }
        RuntimeData.labPipeConfig.dbUser = when {
            properties.containsKey(StaticValue.PROPS_FIELD_DB_USER) -> properties.getString(StaticValue.PROPS_FIELD_DB_USER)
            else -> null
        }
        RuntimeData.labPipeConfig.dbPass = when {
            properties.containsKey(StaticValue.PROPS_FIELD_DB_PASS) -> properties.getString(StaticValue.PROPS_FIELD_DB_PASS)
            else -> null
        }
        RuntimeData.labPipeConfig.emailHost = when {
            properties.containsKey(StaticValue.PROPS_FIELD_EMAIL_HOST) -> properties.getString(StaticValue.PROPS_FIELD_EMAIL_HOST)
            else -> "localhost"
        }
        RuntimeData.labPipeConfig.emailPort = when {
            properties.containsKey(StaticValue.PROPS_FIELD_EMAIL_PORT) -> properties.getInt(StaticValue.PROPS_FIELD_EMAIL_PORT)
            else -> 25
        }
        RuntimeData.labPipeConfig.emailUser = when {
            properties.containsKey(StaticValue.PROPS_FIELD_EMAIL_USER) -> properties.getString(StaticValue.PROPS_FIELD_EMAIL_USER)
            else -> null
        }
        RuntimeData.labPipeConfig.emailPass = when {
            properties.containsKey(StaticValue.PROPS_FIELD_EMAIL_PASS) -> properties.getString(StaticValue.PROPS_FIELD_EMAIL_PASS)
            else -> null
        }
        RuntimeData.labPipeConfig.notificationEmailName = when {
            properties.containsKey(StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_NAME) -> properties.getString(StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_NAME)
            else -> "LabPipe Notification"
        }
        RuntimeData.labPipeConfig.notificationEmailAddress = when {
            properties.containsKey(StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR) -> properties.getString(StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR)
            else -> "no-reply@labpipe.org"
        }
    }
}

fun startServer(port: Int) {
    DatabaseUtil.connect()
    EmailUtil.connect()
    AuthManager.setManager()
    GeneralService.routes()
    ParameterService.routes()
    RecordService.routes()
    FormService.routes()
    DevService.routes()
    RuntimeData.labPipeServer.start(port)
}

class LPServerCmdLine : CliktCommand() {

    val action by option("--action", help = "server actions").choice("start", "stop", "restart")

    val configPath by option("--config", help = "config file path")

    val serverPort by option("--port", help = "server port").int().default(4567)

    val dbHost by option("--db-host", help = "database host")
    val dbPort by option("--db-port", help = "database port").int()
    val dbName by option("--db-name", help = "database name")
    val dbUser by option("--db-user", help = "database user")
    val dbPass by option("--db-pass", help = "database password")

    val emailHost by option("--email-host", help = "email host")
    val emailPort by option("--email-port", help = "email port").int()
    val emailUser by option("--email-user", help = "email user")
    val emailPass by option("--email-pass", help = "email password")
    val notificationEmailName by option("--email-notification-name", help = "notification email sender name")
    val notificationEmailAddress by option("--email-notification-address", help = "notification email sender address")

    val cacheDir by option("--cache-dir", help = "cache directory")

    override fun run() {
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_DB_HOST, value = dbHost)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_DB_PORT, value = dbPort)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_DB_NAME, value = dbName)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_DB_USER, value = dbUser)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_DB_PASS, value = dbPass)

        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_EMAIL_HOST, value = emailHost)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_EMAIL_PORT, value = emailPort)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_EMAIL_USER, value = emailUser)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_EMAIL_PASS, value = emailPass)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_NAME, value = notificationEmailName)
        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_EMAIL_NOTIFICATION_ADDR, value = notificationEmailAddress)

        updateConfig(path = configPath, key = StaticValue.PROPS_FIELD_PATH_CACHE, value = cacheDir)

        when(action) {
            "start" -> {
                importConfig(configPath)
                startServer(serverPort)
            }
            else -> echo(action)
        }
    }

}