package uk.ac.le.ember.labpipe.server.db

import com.mongodb.MongoClientURI
import org.litote.kmongo.*
import uk.ac.le.ember.labpipe.server.sessions.RuntimeData

class DBConnector {

    companion object {

        fun connect() {
            val mongoConnectionString = if (RuntimeData.labPipeConfig.dbUser != null && RuntimeData.labPipeConfig.dbPass != null)
                "mongodb://${RuntimeData.labPipeConfig.dbUser}:${RuntimeData.labPipeConfig.dbPass}@${RuntimeData.labPipeConfig.dbHost}:${RuntimeData.labPipeConfig.dbPort}/${RuntimeData.labPipeConfig.dbName}"
            else
                "mongodb://${RuntimeData.labPipeConfig.dbHost}:${RuntimeData.labPipeConfig.dbPort}/${RuntimeData.labPipeConfig.dbName}"
            val mongoUri = MongoClientURI(mongoConnectionString)
            RuntimeData.mongoClient = KMongo.createClient(mongoUri)
            RuntimeData.mongoDatabase = RuntimeData.mongoClient.getDatabase(RuntimeData.labPipeConfig.dbName)
        }
    }
}