package uk.ac.le.ember.labpipe.server.controllers

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.source.toml.toToml
import mu.KotlinLogging
import uk.ac.le.ember.labpipe.server.DEFAULT_CONFIG_FILE_NAME
import java.io.File
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

class ConfigController {

    companion object {
        object LabPipeConfig : ConfigSpec("labpipe") {
            val port by optional<Int>(4567)
            val showBrowsableApi by optional<Boolean>(true)
            val showDefaultPage by optional<Boolean>(true)
            val defaultPageDirectory by optional<String>("")
            object Database : ConfigSpec("database") {
                val host by optional<String>("localhost")
                val port by optional<Int>(27017)
                val name by optional<String>("labpipe")
                val user by optional<String>("")
                val password by optional<String>("")
                val useSrv by optional<Boolean>(false)
            }

            object Email : ConfigSpec("email") {
                val host by optional<String>("localhost")
                val port by optional<Int>(25)
                val user by optional<String>("")
                val password by optional<String>("")
                val fromName by optional<String>("LabPipe Notification")
                val fromAddress by optional<String>("no-reply@labpipe.org")
            }

            object Security : ConfigSpec("security") {
                val enforceSsl by optional<Boolean>(true)
                val rateLimitPublic by optional<Int>(100)
            }

            object Storage : ConfigSpec("storage") {
                val cache by optional<String>(Paths.get(System.getProperty("user.home"), "labpipe").toString())
                val upload by optional<String>(Paths.get(System.getProperty("user.home"), "labpipe", "uploaded").toString())
                val parts by optional<String>(Paths.get(System.getProperty("user.home"), "labpipe", "parts").toString())
            }

            object Parameter: ConfigSpec("parameter") {
                val manifest by optional<MutableSet<String>>(mutableSetOf())
            }

            object BioPortal: ConfigSpec("bioportal") {
                val api by optional<String>("")
            }
        }

        fun load(path: String = DEFAULT_CONFIG_FILE_NAME): Config {
            var config = Config {
                addSpec(LabPipeConfig)
            }
            if (!File(path).exists()) {
                logger.info { "Config file not found at $path."}
                config.toToml.toFile(path)
                logger.info { "A default config file is generated at $path."}
            } else {
                config = config.from.toml.file(path)
                config.toToml.toFile(path)
            }
            config.validateRequired()
            return config
        }
    }
}