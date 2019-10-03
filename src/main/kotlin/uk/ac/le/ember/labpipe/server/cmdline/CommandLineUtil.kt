package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import io.javalin.Javalin
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import org.apache.commons.lang3.RandomStringUtils
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.mindrot.jbcrypt.BCrypt
import org.simplejavamail.email.Recipient
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.EmailTemplates
import uk.ac.le.ember.labpipe.server.data.AccessToken
import uk.ac.le.ember.labpipe.server.data.LPConfig
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.db.DatabaseUtil
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.services.*
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.io.File
import java.nio.file.Paths
import java.util.*


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
    GeneralService.routes()
    ParameterService.routes()
    RecordService.routes()
    FormService.routes()
    QueryService.routes()
    ManageService.routes()
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

class Create : CliktCommand(name = "create", help = "Create new record") {

    override fun run() {
        echo("Create new record")
    }
}

class CreateOperator : CliktCommand(name = "operator", help = "Create new operator") {
    private val name by option("--name", help = "operator name").prompt(text = "Please enter operator name")
    private val email by option("--email", help = "operator email").prompt(text = "Please enter operator email")
    override fun run() {
        echo("Operator name: $name")
        echo("Operator email: $email")
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        EmailUtil.testConnection()
        DatabaseUtil.testConnection()
        var currentOperator =
            Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS)
                .findOne(Operator::email eq email)
        if (currentOperator != null) {
            echo("Operator with email [$email] already exists.")
        } else {
            var operator = Operator(email = email)
            operator.name = name
            operator.username = email
            val tempPassword = RandomStringUtils.randomAlphanumeric(8)
            operator.passwordHash = BCrypt.hashpw(tempPassword, BCrypt.gensalt())
            operator.active = true
            Runtime.mongoDatabase.getCollection<Operator>(Constants.MONGO.REQUIRED_COLLECTIONS.OPERATORS).insertOne(operator)
            echo("Operator is created with temporary password: $tempPassword")
            EmailUtil.sendEmail(
                from = Recipient(
                    Runtime.config.notificationEmailName,
                    Runtime.config.notificationEmailAddress,
                    null
                ),
                to = listOf(
                    Recipient(
                        operator.name,
                        operator.email,
                        null
                    )
                ),
                subject = "Your LabPipe Operator Account",
                text = String.format(EmailTemplates.CREATE_OPERATOR_TEXT, operator.name, operator.email, tempPassword),
                html = String.format(EmailTemplates.CREATE_OPERATOR_HTML, operator.name, operator.email, tempPassword),
                async = true
            )
        }

    }
}

class CreateAccessToken : CliktCommand(name = "token", help = "Create new access token") {
    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        EmailUtil.testConnection()
        DatabaseUtil.testConnection()
        var token = UUID.randomUUID().toString()
        while (Runtime.mongoDatabase.getCollection<AccessToken>(Constants.MONGO.REQUIRED_COLLECTIONS.ACCESS_TOKENS)
                .findOne(AccessToken::token eq token) != null) {
            token = UUID.randomUUID().toString()
        }
        var key = RandomStringUtils.randomAlphanumeric(16)
        var accessToken = AccessToken(token = token, keyHash = BCrypt.hashpw(key, BCrypt.gensalt()))
            Runtime.mongoDatabase.getCollection<AccessToken>(Constants.MONGO.REQUIRED_COLLECTIONS.ACCESS_TOKENS).insertOne(accessToken)
            echo("Access token created.")
        echo("Token: $token")
        echo("Key: $key")

    }
}

class Import : CliktCommand(name = "import", help = "Import record") {
    private val study by option("--study").file(exists = true, fileOkay = true, folderOkay = true, readable = true)


    override fun run() {
        echo("Import record")
    }
}

class Init : CliktCommand(name = "init", help = "Init server") {

    override fun run() {
        echo("The init function will be available in next release.")
    }
}