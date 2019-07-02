package uk.ac.le.ember.labpipe.server.sessions

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import io.javalin.Javalin
import org.simplejavamail.mailer.Mailer
import uk.ac.le.ember.labpipe.server.data.LPConfig

class RuntimeData {
    companion object {
        var labPipeConfig = LPConfig()
        var labPipeServer: Javalin = Javalin.create()

        lateinit var mongoClient: MongoClient
        lateinit var mongoDatabase: MongoDatabase
        lateinit var mailer: Mailer
    }
}