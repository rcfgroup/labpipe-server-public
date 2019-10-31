package uk.ac.le.ember.labpipe.server.sessions

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import io.javalin.Javalin
import mu.KotlinLogging
import org.simplejavamail.mailer.Mailer
import uk.ac.le.ember.labpipe.server.LPConfig

class Runtime {
    companion object {
        var logger = KotlinLogging.logger {}
        var lpConfig = LPConfig()
        var debugMode: Boolean = false

        lateinit var mongoClient: MongoClient
        lateinit var mongoDatabase: MongoDatabase
        lateinit var mailer: Mailer
        lateinit var server: Javalin
    }
}