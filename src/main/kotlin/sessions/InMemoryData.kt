package sessions

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import configs.LabPipeConfig
import io.javalin.Javalin

class InMemoryData {
    companion object {
        var labPipeConfig = LabPipeConfig()
        var labPipeServer: Javalin = Javalin.create()

        lateinit var mongoClient: MongoClient
        lateinit var mongoDatabase: MongoDatabase
    }
}