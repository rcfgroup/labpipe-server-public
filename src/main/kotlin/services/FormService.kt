package services

import auths.AuthManager
import data.ParamFormTemplate
import db.DatabaseConnector
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import sessions.InMemoryData

object FormService {
    fun routes() {
        println("Add form service routes")
        InMemoryData.labPipeServer.get("/api/form/template/:formCode", { ctx -> getFormTemplate(ctx.pathParam("formCode"))?.let { ctx.json(it) } },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }


    fun getFormTemplate(formCode: String): ParamFormTemplate? {
        val col = InMemoryData.mongoDatabase.getCollection<ParamFormTemplate>("FORM_TEMPLATES")
        return col.findOne(ParamFormTemplate::code eq formCode)
    }
}