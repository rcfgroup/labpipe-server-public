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