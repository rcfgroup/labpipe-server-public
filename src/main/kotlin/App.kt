import auths.AuthManager
import commandline.LabPipeServerCommandLine
import configs.LabPipeConfig
import db.DatabaseConnector
import picocli.CommandLine
import services.DevService
import services.GeneralService
import services.ParameterService
import services.RecordService
import java.nio.file.Paths
import io.javalin.Javalin


class App : Runnable {

    override fun run() {
        val config = LabPipeConfig(systemTemporaryDirectory)
        config.dbHost = dbHost
        config.dbPort = dbPort
        config.dbName = dbName
        config.dbUser = dbUser
        config.dbPass = dbPass
        config.emailHost = emailHost
        config.emailPort = emailPort
        config.emailUser = emailUser
        config.emailPass = emailPass

        DatabaseConnector.connect(config)
        if (actionTest) {
//            loadConfigurations()
        } else if (actionRun) {
            val app = Javalin.create()
            AuthManager.setManager(app)
            GeneralService.routes(app)
            ParameterService.routes(app)
            RecordService.routes(app)
            DevService.routes(app)
            app.start(4567)
        }
    }
}


fun main(args: Array<String>) {
    LabPipeServerCommandLine().main(args)
}