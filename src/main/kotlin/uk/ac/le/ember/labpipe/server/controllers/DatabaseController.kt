package uk.ac.le.ember.labpipe.server.controllers

import com.uchuhimo.konf.Config
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import uk.ac.le.ember.labpipe.server.API
import uk.ac.le.ember.labpipe.server.ApiAccessRole
import uk.ac.le.ember.labpipe.server.ClientSettings
import uk.ac.le.ember.labpipe.server.DEFAULT_ADMIN_ROLE
import uk.ac.le.ember.labpipe.server.DEFAULT_CLIENT_SETTING
import uk.ac.le.ember.labpipe.server.DEFAULT_OPERATOR_ROLE
import uk.ac.le.ember.labpipe.server.DEFAULT_TOKEN_ROLE
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.OperatorRole
import uk.ac.le.ember.labpipe.server.controllers.ConfigController.Companion.LabPipeConfig.Database
import uk.ac.le.ember.labpipe.server.sessions.Runtime

private val logger = KotlinLogging.logger {}

class DatabaseController {

    companion object {

        fun connect(config: Config = Runtime.config) {
            val mongoProtocol = if (config[Database.useSrv]) "mongodb+srv://" else "mongodb://"
            val mongoPort = if (config[Database.useSrv]) "" else ":${config[Database.port]}"
            val mongoConnectionString = if (config[Database.user].isNotEmpty() && config[Database.password].isNotEmpty())
                "${mongoProtocol}${config[Database.user]}:${config[Database.password]}@${config[Database.host]}${mongoPort}/${config[Database.name]}?retryWrites=true&w=majority"
            else
                "${mongoProtocol}${config[Database.host]}${mongoPort}/${config[Database.name]}?retryWrites=true&w=majority"
            Runtime.mongoClient = KMongo.createClient(mongoConnectionString)
            Runtime.mongoDatabase = Runtime.mongoClient.getDatabase(config[Database.name])
        }

        fun testConnection(config: Config = Runtime.config): Boolean {
            connect(config)
            return try {
                Runtime.mongoDatabase.listCollectionNames()
                logger.info { "Database connection successful." }
                true
            } catch (e: Exception) {
                logger.error(e) { "Cannot connect to database." }
                false
            }
        }

        fun init() {
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
}