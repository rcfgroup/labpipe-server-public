package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.google.gson.Gson
import uk.ac.le.ember.labpipe.server.EmailGroup
import uk.ac.le.ember.labpipe.server.Instrument
import uk.ac.le.ember.labpipe.server.Location
import uk.ac.le.ember.labpipe.server.Operator
import uk.ac.le.ember.labpipe.server.OperatorRole
import uk.ac.le.ember.labpipe.server.Study
import uk.ac.le.ember.labpipe.server.controllers.ConfigController
import uk.ac.le.ember.labpipe.server.controllers.DatabaseController
import uk.ac.le.ember.labpipe.server.controllers.EmailController
import uk.ac.le.ember.labpipe.server.services.addEmailGroup
import uk.ac.le.ember.labpipe.server.services.addInstrument
import uk.ac.le.ember.labpipe.server.services.addLocation
import uk.ac.le.ember.labpipe.server.services.addOperator
import uk.ac.le.ember.labpipe.server.services.addRole
import uk.ac.le.ember.labpipe.server.services.addStudy
import uk.ac.le.ember.labpipe.server.services.addToken
import java.io.FileReader

class Add : CliktCommand(name = "add", help = "Add new record") {

    override fun run() {
        echo("Add new record")
    }
}

class AddOperator : CliktCommand(name = "operator", help = "Add new operator") {
    private val name by option("--name", help = "operator name").prompt(text = "Please enter operator name")
    private val email by option("--email", help = "operator email").prompt(text = "Please enter operator email")
    private val show by option("--show", help = "show operator username and password once created").flag()
    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        val result = addOperator(email = email, name = name, notify = true, show = show)
        echo(result.message.message)

    }
}

class AddAccessToken : CliktCommand(name = "token", help = "Add new access token") {
    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        addToken()

    }
}

class AddRole : CliktCommand(name = "role", help = "Add new role") {
    private val identifier by option(
        "--identifier",
        help = "role identifier"
    ).prompt(text = "Please enter role identifier")
    private val name by option("--name", help = "role name").prompt(text = "Please enter role name")

    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        addRole(identifier = identifier, name = name)

    }
}

class AddEmailGroup : CliktCommand(name = "email-group", help = "Add new email group") {
    private val identifier by option(
        "--identifier",
        help = "email group identifier"
    ).prompt(text = "Please enter email group identifier")
    private val name by option("--name", help = "email group name").prompt(text = "Please enter email group name")
    private val formIdentifier by option(
        "--form-identifier",
        help = "email group form identifier"
    ).prompt(text = "Please enter email group form identifier")

    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        addEmailGroup(identifier = identifier, name = name, formIdentifier = formIdentifier)

    }
}

class AddInstrument : CliktCommand(name = "instrument", help = "Add new instrument") {
    private val identifier by option(
        "--identifier",
        help = "instrument identifier"
    ).prompt(text = "Please enter instrument identifier")
    private val name by option("--name", help = "instrument name").prompt(text = "Please enter instrument name")
    private val realtime by option("--realtime", help = "real-time processing instrument").flag(
        "--non-realtime",
        default = false
    )
    private val fileType by option("--file-type", help = "instrument generated file types").split(",")

    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        addInstrument(identifier = identifier, name = name, realtime = realtime, fileType = fileType!!.toMutableList())
    }
}

class AddLocation : CliktCommand(name = "location", help = "Add new location") {
    private val identifier by option(
        "--identifier",
        help = "location identifier"
    ).prompt(text = "Please enter location identifier")
    private val name by option("--name", help = "location name").prompt(text = "Please enter location name")
    private val type by option("--type", help = "location types").split(",").default(mutableListOf())

    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        val location = Location(identifier = identifier, name = name)
        location.type = type.toMutableSet()
        addLocation(location = location)
    }
}

class AddStudy : CliktCommand(name = "study", help = "Add new study") {
    private val identifier by option(
        "--identifier",
        help = "study identifier"
    ).prompt(text = "Please enter study identifier")
    private val name by option("--name", help = "study name").prompt(text = "Please enter study name")
    private val config by option("--config")

    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        val study = Study(identifier = identifier)
        study.name = name
        addStudy(study, config = config)
    }
}

class ImportCmd : CliktCommand(name = "import", help = "Import record(s) from file") {
    private val target by option("--target", help = "import to").choice(
        "operator",
        "role",
        "email-group",
        "instrument",
        "location",
        "study"
    ).prompt(text = "Please enter import target from available choices")
    private val source by option("--source", help = "file of operator(s)").file(
        exists = true,
        fileOkay = true,
        folderOkay = false,
        readable = true
    ).prompt(text = "Please enter source file path")

    override fun run() {
        ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        val gson = Gson()
        when (target) {
            "operator" -> {
                val reader = FileReader(source)
                val data = gson.fromJson(reader, Array<Operator>::class.java)
                data.forEach { addOperator(operator = it, notify = true) }
            }
            "role" -> {
                val reader = FileReader(source)
                val data = gson.fromJson(reader, Array<OperatorRole>::class.java)
                data.forEach { addRole(role = it, notify = true) }
            }
            "email-group" -> {
                val reader = FileReader(source)
                val data = gson.fromJson(reader, Array<EmailGroup>::class.java)
                data.forEach { addEmailGroup(emailGroup = it, notify = true) }
            }
            "instrument" -> {
                val reader = FileReader(source)
                val data = gson.fromJson(reader, Array<Instrument>::class.java)
                data.forEach { addInstrument(instrument = it, notify = true) }
            }
            "location" -> {
                val reader = FileReader(source)
                val data = gson.fromJson(reader, Array<Location>::class.java)
                data.forEach { addLocation(location = it, notify = true) }
            }
            "study" -> {
                val reader = FileReader(source)
                val data = gson.fromJson(reader, Array<Study>::class.java)
                data.forEach { addStudy(study = it, notify = true) }
            }
        }
    }
}