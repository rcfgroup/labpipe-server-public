package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import mu.KotlinLogging
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.controllers.ConfigController
import uk.ac.le.ember.labpipe.server.controllers.DatabaseController
import uk.ac.le.ember.labpipe.server.controllers.EmailController
import uk.ac.le.ember.labpipe.server.routes.RouteController
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun startServer() {
    Runtime.server = Javalin.create { config ->
        config.enableCorsForAllOrigins()
        config.enforceSsl = Runtime.config[ConfigController.Companion.LabPipeConfig.Security.enforceSsl]
    }
    AuthManager.setManager()
    RouteController.addRoutes()
    Runtime.server.start(Runtime.config[ConfigController.Companion.LabPipeConfig.port])
    logger.info("Server running at ${Runtime.config[ConfigController.Companion.LabPipeConfig.port]}")
}

class LPServerCmdLine :
    CliktCommand(name = "LabPipe Server Commandline Tool", help = "LabPipe Server Commandline Tool") {
    override fun run() {
        echo("LabPipe Server")
    }

}

class Check : CliktCommand(name = "check", help = "Check database/email connection", treatUnknownOptionsAsArgs = true) {
    override fun run() {
        Runtime.config = ConfigController.load()
        EmailController.testConnection()
        DatabaseController.testConnection()
        exitProcess(0)
    }
}

class Run : CliktCommand(name = "run", help = "Run server", treatUnknownOptionsAsArgs = true) {
    private val debugMode by option("--debug", help = "debug mode").flag()
    // TODO enable daemon mode

    override fun run() {
        Runtime.debugMode = debugMode
        Runtime.config = ConfigController.load()
        DatabaseController.connect()
        EmailController.connect()
        Runtime.emailAvailable = EmailController.testConnection()
        DatabaseController.testConnection()
        startServer()
    }
}

class Init : CliktCommand(name = "init", help = "Init server", treatUnknownOptionsAsArgs = true) {

    override fun run() {
        Runtime.config = ConfigController.load()
        DatabaseController.connect()
        DatabaseController.testConnection()
        DatabaseController.init()
    }
}