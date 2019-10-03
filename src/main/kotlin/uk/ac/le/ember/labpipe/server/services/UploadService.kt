package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.util.FileUtil
import io.javalin.http.Context
import org.apache.commons.io.FilenameUtils
import uk.ac.le.ember.labpipe.server.data.Message
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.nio.file.Paths

private fun uploadFile(ctx: Context): Context {
    val identifier = ctx.queryParam("identifier")
    identifier?.run {
        ctx.uploadedFiles("files").forEachIndexed { index, uploadedFile ->
            FileUtil.streamToFile(
                uploadedFile.content, Paths.get(
                    Runtime.config.uploadedPath,
                    "${FilenameUtils.getBaseName(uploadedFile.filename)}@${index}.${uploadedFile.extension}"
                ).toString()
            )
            return ctx.json(Message("Uploaded."))
        }
    }
    ctx.status(400)
    return ctx.json(Message("Form action identifier is required."))
}

fun routes() {

}