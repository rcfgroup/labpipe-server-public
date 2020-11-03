import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
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
import uk.ac.le.ember.labpipe.server.controllers.DatabaseController
import uk.ac.le.ember.labpipe.server.sessions.Runtime

class DatabaseConnectionTest : FunSpec({
    test("Test database connection") {
        DatabaseController.testConnection(testConfig).shouldBeTrue()
    }
})

class DatabaseInitTest: FunSpec({

    beforeSpec {
        DatabaseController.connect(testConfig)
        DatabaseController.init()
    }

    test("Test create default token role") {
        MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq DEFAULT_TOKEN_ROLE.identifier).shouldNotBeNull()
    }

    test("Test create default operator role") {
        MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq DEFAULT_OPERATOR_ROLE.identifier).shouldNotBeNull()
    }

    test("Test create default admin role") {
        MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq DEFAULT_ADMIN_ROLE.identifier).shouldNotBeNull()
    }

    test("Test create default client setting") {
        MONGO.COLLECTIONS.CLIENT_SETTINGS.findOne(ClientSettings::identifier eq DEFAULT_CLIENT_SETTING.identifier).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.GENERAL.CONN_AUTH}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.GENERAL.CONN_AUTH).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.GENERAL.CONN_TOKEN}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.GENERAL.CONN_TOKEN).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.PARAMETER.FROM_NAME}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.PARAMETER.FROM_NAME).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.FORM.ALL}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.FORM.ALL).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.FORM.FROM_IDENTIFIER}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.FORM.FROM_IDENTIFIER).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.FORM.FROM_STUDY_INSTRUMENT}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.FORM.FROM_STUDY_INSTRUMENT).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.MANAGE.CREATE.INSTRUMENT}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.MANAGE.CREATE.INSTRUMENT).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.MANAGE.CREATE.LOCATION}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.MANAGE.CREATE.LOCATION).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.MANAGE.CREATE.OPERATOR}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.MANAGE.CREATE.OPERATOR).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.MANAGE.CREATE.ROLE}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.MANAGE.CREATE.ROLE).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.MANAGE.CREATE.STUDY}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.MANAGE.CREATE.STUDY).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.MANAGE.CREATE.TOKEN}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.MANAGE.CREATE.TOKEN).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.MANAGE.UPDATE.PASSWORD}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.MANAGE.UPDATE.PASSWORD).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.QUERY.INSTRUMENT}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.QUERY.INSTRUMENT).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.QUERY.INSTRUMENTS}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.QUERY.INSTRUMENTS).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.QUERY.RECORDS}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.QUERY.RECORDS).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.QUERY.STUDY_RECORDS}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.QUERY.STUDY_RECORDS).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.QUERY.STUDY}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.QUERY.STUDY).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.QUERY.STUDIES}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.QUERY.STUDIES).shouldNotBeNull()
    }

    test("Test create api access roles for ${API.RECORD.ADD}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.RECORD.ADD).shouldNotBeNull()
    }


    test("Test create api access roles for ${API.UPLOAD.FORM_FILE}") {
        MONGO.COLLECTIONS.API_ACCESS_ROLES.findOne(ApiAccessRole::url eq API.UPLOAD.FORM_FILE).shouldNotBeNull()
    }
})