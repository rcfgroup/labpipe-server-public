package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.util.FileUtil
import io.javalin.http.Context
import org.apache.commons.codec.digest.DigestUtils.md5Hex
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import uk.ac.le.ember.labpipe.server.FormFileChunkUpload
import uk.ac.le.ember.labpipe.server.FormFileUpload
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.Message
import uk.ac.le.ember.labpipe.server.controllers.ConfigController.Companion.LabPipeConfig.Storage
import uk.ac.le.ember.labpipe.server.sessions.Runtime
import java.nio.file.Files
import java.nio.file.Paths

fun uploadFile(ctx: Context): Context {
    val identifier = ctx.queryParam("identifier")
    identifier?.run {
        val f = FormFileUpload(identifier = identifier)
        ctx.uploadedFiles("files").forEachIndexed { index, uploadedFile ->
            val newPath = Paths.get(
                Runtime.config[Storage.upload],
                "${identifier}@${index}${uploadedFile.extension}"
            )
            FileUtil.streamToFile(
                uploadedFile.content, newPath.toString()
            )
            f.files.add(newPath.toString())
        }
        MONGO.COLLECTIONS.UPLOADED.insertOne(f)
        return ctx.status(200).json(Message("Uploaded completed."))
    }
    return ctx.status(400).json(Message("Form action identifier is required."))
}

fun uploadFileChunk(ctx: Context): Context {
    val identifier = ctx.queryParam("identifier")
    val chunk = ctx.queryParam<Int>("chunk").get()
    val total = ctx.queryParam<Int>("total").get()
    val md5Chunk = ctx.formParam<String>("md5Chunk").get()
    val md5Total = ctx.formParam<String>("md5Total").get()
    val ext = ctx.queryParam<String>("ext").get()
    identifier?.run {
        val f = FormFileChunkUpload(identifier = identifier, chunk = chunk, total = total, md5Chunk = md5Chunk, md5Total = md5Total, ext = ext)
        if (MONGO.COLLECTIONS.CHUNKED.findOne(and(FormFileChunkUpload::identifier eq identifier, FormFileChunkUpload::chunk eq chunk, FormFileChunkUpload::md5Total eq md5Total)) != null) {
            return ctx.status(400).json(Message("File chunk already exists on server."))
        }
        if (ctx.uploadedFiles("files").size != 1) {
            return ctx.status(400).json(Message("File chunk upload must have only one file uploaded each time."))
        }
        val newPath = Paths.get(
            Runtime.config[Storage.upload],
            "${identifier}@${total}-${chunk}.bin"
        )
        FileUtil.streamToFile(
            ctx.uploadedFiles("files")[0].content, newPath.toString()
        )
        val inputStream = Files.newInputStream(newPath)
        if (md5Hex(inputStream) != md5Chunk) {
            return ctx.status(400).json(Message("File md5 does not match."))
        }
        f.file = newPath.toString()
        MONGO.COLLECTIONS.CHUNKED.insertOne(f)
        return ctx.status(200).json(Message("Uploaded completed."))
    }
    return ctx.status(400).json(Message("Form action identifier is required."))
}