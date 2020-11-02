import com.uchuhimo.konf.Config
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.spec.AutoScan
import uk.ac.le.ember.labpipe.server.controllers.ConfigController
import uk.ac.le.ember.labpipe.server.sessions.Runtime

public var testConfig: Config = Config {
    addSpec(ConfigController.Companion.LabPipeConfig)
}

@AutoScan
object PrepareProjectListener : ProjectListener {
    override suspend fun beforeProject() {
        testConfig[ConfigController.Companion.LabPipeConfig.Database.name] = "labpipe-test"
        super.beforeProject()
    }

    override suspend fun afterProject() {
        Runtime.mongoDatabase.drop()
        super.afterProject()
    }
}