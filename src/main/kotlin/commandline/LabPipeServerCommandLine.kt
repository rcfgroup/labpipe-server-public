package commandline

import auths.AuthManager
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import configs.LabPipeConfig
import db.DatabaseConnector
import io.javalin.Javalin
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import services.DevService
import services.GeneralService
import services.ParameterService
import services.RecordService
import sessions.InMemoryData
import java.io.File
import java.nio.file.Paths

fun updateConfig(path:String? = null, key: String, value: String?) {
    val configFilePath = path ?: "config.ini"
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
    val configFilePath = path ?: "config.ini"
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
    val configFilePath = path ?: "config.ini"
    val configFile = File(configFilePath)
    val configs = Configurations()
    if (!configFile.exists()) {
        updateConfig(path = path, key = "database.host", value = "localhost")
        updateConfig(path = path, key = "database.port", value = 27017)
        updateConfig(path = path, key = "database.name", value = "labpipe-dev")

        updateConfig(path = path, key = "mail.host", value = "localhost")
        updateConfig(path = path, key = "mail.port", value = 25)

        val defaultCacheDir = Paths.get(System.getProperty("user.home"), "labpipe").toString()
        updateConfig(path = path, key = "path.cache", value = defaultCacheDir)

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

fun checkConfig(configPath: String?) {
    val properties = readConfig(configPath)
    val config = LabPipeConfig(properties?.getString("path.cache") ?: Paths.get(System.getProperty("user.home"), "labpipe").toString())
    config.dbHost = properties?.getString("database.host") ?: "localhost"
    config.dbPort = properties?.getInt("database.port") ?: 27017
    config.dbName = properties?.getString("database.name") ?: "labpipe-dev"
    config.dbUser = properties?.getString("database.user")
    config.dbPass = properties?.getString("database.pass")
    config.emailHost = properties?.getString("mail.host") ?: "localhost"
    config.emailPort = properties?.getInt("mail.port") ?: 25
    config.emailUser = properties?.getString("mail.user")
    config.emailPass = properties?.getString("mail.pass")
    InMemoryData.labPipeConfig = config
}

fun startServer(configPath: String?) {
    checkConfig(configPath)
    DatabaseConnector.connect(InMemoryData.labPipeConfig)
    val app = Javalin.create()
    AuthManager.setManager(app)
    GeneralService.routes(app)
    ParameterService.routes(app)
    RecordService.routes(app)
    DevService.routes(app)
    app.start(4567)
}

class LabPipeServerCommandLine : CliktCommand() {
    val action by option("--action", help = "server actions").choice("start", "stop", "restart")

    val configPath by option("--config", help = "config file path")

    val dbHost by option("--db-host", help = "database host")
    val dbPort by option("--db-port", help = "database port").int()
    val dbName by option("--db-name", help = "database name")
    val dbUser by option("--db-user", help = "database user")
    val dbPass by option("--db-pass", help = "database password")

    val emailHost by option("--email-host", help = "email host")
    val emailPort by option("--email-port", help = "email port").int()
    val emailUser by option("--email-user", help = "email user")
    val emailPass by option("--email-pass", help = "email password")

    val cacheDir by option("--cache-dir", help = "cache directory")

    override fun run() {
        updateConfig(path = configPath, key = "database.host", value = dbHost)
        updateConfig(path = configPath, key = "database.port", value = dbPort)
        updateConfig(path = configPath, key = "database.name", value = dbName)
        updateConfig(path = configPath, key = "database.user", value = dbUser)
        updateConfig(path = configPath, key = "database.pass", value = dbPass)

        updateConfig(path = configPath, key = "mail.host", value = emailHost)
        updateConfig(path = configPath, key = "mail.port", value = emailPort)
        updateConfig(path = configPath, key = "mail.user", value = emailUser)
        updateConfig(path = configPath, key = "mail.pass", value = emailPass)

        updateConfig(path = configPath, key = "path.cache", value = cacheDir)

        when(action) {
            "start" -> startServer(configPath)
            else -> echo(action)
        }
    }

}