package db

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import configs.LabPipeConfig
import org.litote.kmongo.*

class DatabaseConnector {

    companion object {
        lateinit var client: MongoClient
        lateinit var database: MongoDatabase

        fun connect(c: LabPipeConfig) {
            val mongoConnectionString = if (c.dbUser != null && c.dbPass != null)
                "mongodb://${c.dbUser}:${c.dbPass}@${c.dbHost}:${c.dbPort}/${c.dbName}"
            else
                "mongodb://${c.dbHost}:${c.dbPort}/${c.dbName}"
            val mongoUri = MongoClientURI(mongoConnectionString)
            client = KMongo.createClient(mongoUri) //get com.mongodb.MongoClient new instance
            database = client.getDatabase("bz-test") //normal java driver usage
        }
    }
}