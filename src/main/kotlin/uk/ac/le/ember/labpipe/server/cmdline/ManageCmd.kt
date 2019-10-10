package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import uk.ac.le.ember.labpipe.server.db.DatabaseUtil
import uk.ac.le.ember.labpipe.server.notification.EmailUtil
import uk.ac.le.ember.labpipe.server.services.ManageService.addEmailGroup
import uk.ac.le.ember.labpipe.server.services.ManageService.addOperator
import uk.ac.le.ember.labpipe.server.services.ManageService.addRole
import uk.ac.le.ember.labpipe.server.services.ManageService.addToken

class Add : CliktCommand(name = "add", help = "Add new record") {

    override fun run() {
        echo("Add new record")
    }
}

class AddOperator : CliktCommand(name = "operator", help = "Add new operator") {
    private val name by option("--name", help = "operator name").prompt(text = "Please enter operator name")
    private val email by option("--email", help = "operator email").prompt(text = "Please enter operator email")
    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        val result = addOperator(email = email, name = name, notify = true)
        echo(result.message.message)

    }
}

class AddAccessToken : CliktCommand(name = "token", help = "Add new access token") {
    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        addToken()

    }
}

class AddRole: CliktCommand(name = "role", help = "Add new role") {
    private val identifier by option("--identifier", help = "role identifier").prompt(text = "Please enter role identifier")
    private val name by option("--name", help = "role name").prompt(text = "Please enter role name")

    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        addRole(identifier = identifier, name = name)

    }
}

class AddEmailGroup: CliktCommand(name = "email-group", help = "Add new email group") {
    private val identifier by option("--identifier", help = "email group identifier").prompt(text = "Please enter email group identifier")
    private val name by option("--name", help = "email group name").prompt(text = "Please enter email group name")
    private val formIdentifier by option("--form-identifier", help = "email group form identifier").prompt(text = "Please enter email group form identifier")

    override fun run() {
        importConfig()
        DatabaseUtil.connect()
        EmailUtil.connect()
        addEmailGroup(identifier = identifier, name = name, formIdentifier = formIdentifier)

    }
}