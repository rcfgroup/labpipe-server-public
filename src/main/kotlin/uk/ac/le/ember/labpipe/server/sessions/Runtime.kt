package uk.ac.le.ember.labpipe.server.sessions

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import com.uchuhimo.konf.Config
import io.javalin.Javalin
import org.simplejavamail.api.mailer.Mailer

class Runtime {
    companion object {
        var debugMode: Boolean = false

        lateinit var config: Config
        lateinit var mongoClient: MongoClient
        lateinit var mongoDatabase: MongoDatabase
        lateinit var mailer: Mailer
        lateinit var server: Javalin

        var emailAvailable: Boolean = false;
    }
}