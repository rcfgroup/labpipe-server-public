package uk.ac.le.ember.labpipe.server.sessions

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import uk.ac.le.ember.labpipe.server.configs.LPConfig
import io.javalin.Javalin

class RuntimeData {
    companion object {
        var labPipeConfig = LPConfig()
        var labPipeServer: Javalin = Javalin.create()

        lateinit var mongoClient: MongoClient
        lateinit var mongoDatabase: MongoDatabase
    }
}