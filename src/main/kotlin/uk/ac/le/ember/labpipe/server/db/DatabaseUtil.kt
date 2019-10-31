package uk.ac.le.ember.labpipe.server.db

import com.mongodb.MongoClientURI
import org.litote.kmongo.KMongo
import uk.ac.le.ember.labpipe.server.sessions.Runtime

class DatabaseUtil {

    companion object {

        fun connect() {
            val mongoConnectionString = if (Runtime.lpConfig.dbUser != null && Runtime.lpConfig.dbPass != null)
                "mongodb://${Runtime.lpConfig.dbUser}:${Runtime.lpConfig.dbPass}@${Runtime.lpConfig.dbHost}:${Runtime.lpConfig.dbPort}/${Runtime.lpConfig.dbName}?retryWrites=false"
            else
                "mongodb://${Runtime.lpConfig.dbHost}:${Runtime.lpConfig.dbPort}/${Runtime.lpConfig.dbName}?retryWrites=false"
            val mongoUri = MongoClientURI(mongoConnectionString)
            Runtime.mongoClient = KMongo.createClient(mongoUri)
            Runtime.mongoDatabase = Runtime.mongoClient.getDatabase(Runtime.lpConfig.dbName)
        }

        fun testConnection(): Boolean {
            return try {
                Runtime.mongoDatabase.listCollectionNames()
                Runtime.logger.info { "Database connection successful." }
                true
            } catch (e: Exception) {
                if (Runtime.debugMode) {
                    Runtime.logger.error(e) { "Cannot connect to database." }
                } else {
                    Runtime.logger.error { "Cannot connect to database." }
                }
                false
            }
        }
    }
}