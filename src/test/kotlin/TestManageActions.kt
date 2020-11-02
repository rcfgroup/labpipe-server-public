import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.mindrot.jbcrypt.BCrypt
import uk.ac.le.ember.labpipe.server.AccessToken
import uk.ac.le.ember.labpipe.server.EmailGroup
import uk.ac.le.ember.labpipe.server.Instrument
import uk.ac.le.ember.labpipe.server.Location
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.Operator
import uk.ac.le.ember.labpipe.server.OperatorRole
import uk.ac.le.ember.labpipe.server.Study
import uk.ac.le.ember.labpipe.server.controllers.DatabaseController
import uk.ac.le.ember.labpipe.server.services.addEmailGroup
import uk.ac.le.ember.labpipe.server.services.addInstrument
import uk.ac.le.ember.labpipe.server.services.addLocation
import uk.ac.le.ember.labpipe.server.services.addOperator
import uk.ac.le.ember.labpipe.server.services.addRole
import uk.ac.le.ember.labpipe.server.services.addStudy
import uk.ac.le.ember.labpipe.server.services.addToken
import uk.ac.le.ember.labpipe.server.services.changePassword
import uk.ac.le.ember.labpipe.server.services.generateTokenAndKey

class TestManageActions : FunSpec ({

    var operator: Operator = Operator(email = "test@email.com")
    val password = "password"
    val passwordHash = "cGFzc3dvcmQ=" // password
    val token = "token"
    val newRole = "newRole"
    val newEmailGroup = "newEmailGroup"
    val newForm = "newForm"
    val newInstrument = "newInstrument"
    val newLocation = "newLocation"
    val newStudy = "newStudy"

    beforeSpec {
        DatabaseController.connect(testConfig)
        mockkStatic("uk.ac.le.ember.labpipe.server.services.ManageServiceKt")
        every { generateTokenAndKey() } returns Pair(token, "key")
    }

    test("Test add operator") {
        addOperator(email = "test@email.com", name = "Test Operator", notify = false, show = false)
        val o = MONGO.COLLECTIONS.OPERATORS.findOne(Operator::email eq operator.email)
        o.shouldNotBeNull()
        operator = o
        operator.email.shouldBe("test@email.com")
        operator.name.shouldBe("Test Operator")
    }

    test("Test change password") {
        changePassword(operator = operator, newPassHash = passwordHash)
        val o = MONGO.COLLECTIONS.OPERATORS.findOne(Operator::email eq operator.email)
        BCrypt.checkpw(password, operator.passwordHash).shouldBeTrue()
    }

    test("Test add token") {
        addToken(operator = operator, notify = false)
        MONGO.COLLECTIONS.ACCESS_TOKENS.findOne(AccessToken::token eq token).shouldNotBeNull()
    }

    test("Test add role") {
        addRole(identifier = newRole, name = newRole, operator = operator, notify = false)
        MONGO.COLLECTIONS.ROLES.findOne(OperatorRole::identifier eq newRole).shouldNotBeNull()
    }

    test("Test add email group") {
        addEmailGroup(identifier = newEmailGroup, name = newEmailGroup, formIdentifier = newForm, notify = false)
        MONGO.COLLECTIONS.EMAIL_GROUPS.findOne(EmailGroup::identifier eq newEmailGroup).shouldNotBeNull()
    }

    test("Test add instrument") {
        addInstrument(identifier = newInstrument, name = newInstrument, notify = false)
        MONGO.COLLECTIONS.INSTRUMENTS.findOne(Instrument::identifier eq newInstrument).shouldNotBeNull()
    }

    test("Test add location") {
        addLocation(location = Location(identifier = newLocation, name = newLocation), notify = false)
        MONGO.COLLECTIONS.LOCATIONS.findOne(Location::identifier eq newLocation).shouldNotBeNull()
    }

    test("Test add study") {
        addStudy(study = Study(identifier = newStudy), notify = false)
        MONGO.COLLECTIONS.STUDIES.findOne(Study::identifier eq newStudy).shouldNotBeNull()
    }
})