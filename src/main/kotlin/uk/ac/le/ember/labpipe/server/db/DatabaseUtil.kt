package uk.ac.le.ember.labpipe.server.db

import com.mongodb.MongoClientURI
import org.litote.kmongo.KMongo
import uk.ac.le.ember.labpipe.server.sessions.Runtime

class DatabaseUtil {

    companion object {

        fun connect() {
            val mongoConnectionString = if (Runtime.config.dbUser != null && Runtime.config.dbPass != null)
                "mongodb://${Runtime.config.dbUser}:${Runtime.config.dbPass}@${Runtime.config.dbHost}:${Runtime.config.dbPort}/${Runtime.config.dbName}"
            else
                "mongodb://${Runtime.config.dbHost}:${Runtime.config.dbPort}/${Runtime.config.dbName}"
            val mongoUri = MongoClientURI(mongoConnectionString)
            Runtime.mongoClient = KMongo.createClient(mongoUri)
            Runtime.mongoDatabase = Runtime.mongoClient.getDatabase(Runtime.config.dbName)
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