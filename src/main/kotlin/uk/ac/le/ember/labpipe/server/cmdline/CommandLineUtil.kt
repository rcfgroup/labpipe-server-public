package uk.ac.le.ember.labpipe.server.cmdline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import mu.KotlinLogging
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.controllers.ConfigController
import uk.ac.le.ember.labpipe.server.controllers.DatabaseController
import uk.ac.le.ember.labpipe.server.controllers.EmailController
import uk.ac.le.ember.labpipe.server.routes.RouteController
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

private fun getOpenApiOptions(): OpenApiOptions {
    val applicationInfo: Info = Info()
        .version("1.0")
        .description("LabPipe Server")
    return OpenApiOptions(applicationInfo)
        .path("/docs")
        .swagger(SwaggerOptions("/swagger").title("LabPipe Swagger Documentation"))
        .reDoc(ReDocOptions("/redoc").title("LabPipe ReDoc Documentation"))
}

fun startServer() {
    Runtime.server = Javalin.create { config ->
        config.enableCorsForAllOrigins()
        config.enforceSsl = Runtime.config[ConfigController.Companion.LabPipeConfig.Security.enforceSsl]
        if (Runtime.config[ConfigController.Companion.LabPipeConfig.showBrowsableApi]) {
            config.registerPlugin(OpenApiPlugin(getOpenApiOptions()))
        }
        if (Runtime.config[ConfigController.Companion.LabPipeConfig.showDefaultPage]) {
            if (Runtime.config[ConfigController.Companion.LabPipeConfig.defaultPageDirectory].isEmpty()) {
                config.addStaticFiles("/page")
            } else {
                config.addStaticFiles(Runtime.config[ConfigController.Companion.LabPipeConfig.defaultPageDirectory], Location.EXTERNAL)
            }
        }
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