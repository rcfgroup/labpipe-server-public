package services

import auths.AuthManager
import data.ParamFormTemplate
import db.DatabaseConnector
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

object FormService {
    fun routes(app: Javalin) {
        app.get("/api/form/template/:formCode", { ctx -> getFormTemplate(ctx.pathParam("formCode"))?.let { ctx.json(it) } },
            SecurityUtil.roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }


    fun getFormTemplate(formCode: String): ParamFormTemplate? {
        val col = DatabaseConnector.database.getCollection<ParamFormTemplate>("FORM_TEMPLATES")
        println(col.countDocuments())
        println(col.find().first())
        return col.findOne(ParamFormTemplate::code eq formCode)
    }
}