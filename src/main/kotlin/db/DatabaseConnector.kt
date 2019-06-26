package db

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import configs.LabPipeConfig
import org.litote.kmongo.*
import sessions.InMemoryData

class DatabaseConnector {

    companion object {

        fun connect() {
            val mongoConnectionString = if (InMemoryData.labPipeConfig.dbUser != null && InMemoryData.labPipeConfig.dbPass != null)
                "mongodb://${InMemoryData.labPipeConfig.dbUser}:${InMemoryData.labPipeConfig.dbPass}@${InMemoryData.labPipeConfig.dbHost}:${InMemoryData.labPipeConfig.dbPort}/${InMemoryData.labPipeConfig.dbName}"
            else
                "mongodb://${InMemoryData.labPipeConfig.dbHost}:${InMemoryData.labPipeConfig.dbPort}/${InMemoryData.labPipeConfig.dbName}"
            val mongoUri = MongoClientURI(mongoConnectionString)
            InMemoryData.mongoClient = KMongo.createClient(mongoUri)
            InMemoryData.mongoDatabase = InMemoryData.mongoClient.getDatabase(InMemoryData.labPipeConfig.dbName)
        }
    }
}