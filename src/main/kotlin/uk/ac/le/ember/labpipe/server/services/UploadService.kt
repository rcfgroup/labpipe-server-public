package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil
import io.javalin.core.util.FileUtil
import io.javalin.http.Context
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.AuthManager
import uk.ac.le.ember.labpipe.server.Constants
import uk.ac.le.ember.labpipe.server.data.FormFileUpload
import uk.ac.le.ember.labpipe.server.data.Message
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.nio.file.Paths

object UploadService {
    private fun uploadFile(ctx: Context): Context {
        val identifier = ctx.queryParam("identifier")
        identifier?.run {
            var f = FormFileUpload(identifier = identifier)
            println(ctx.uploadedFiles("files").count())
            ctx.uploadedFiles("files").forEachIndexed { index, uploadedFile ->
                val newPath = Paths.get(
                    Runtime.config.uploadedPath,
                    "${identifier}@${index}${uploadedFile.extension}"
                )
                FileUtil.streamToFile(
                    uploadedFile.content, newPath.toString()
                )
                f.files.add(newPath.toString())
            }
            Runtime.mongoDatabase.getCollection<FormFileUpload>(Constants.MONGO.REQUIRED_COLLECTIONS.UPLOADED).insertOne(f)
            ctx.status(200)
            return ctx.json(Message("Uploaded completed."))
        }
        ctx.status(400)
        return ctx.json(Message("Form action identifier is required."))
    }

    fun routes() {
        println("Add upload service routes.")
        Runtime.server.post(Constants.API.UPLOAD.FORM_FILE, { ctx -> uploadFile(ctx)},
            SecurityUtil.roles(AuthManager.ApiRole.AUTHORISED, AuthManager.ApiRole.TOKEN_AUTHORISED))
    }
}