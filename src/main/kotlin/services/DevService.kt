package services

import auths.AuthManager
import data.ParamFormTemplate
import db.DatabaseConnector
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.litote.kmongo.*

object DevService {
    fun routes(app: Javalin) {
        println("Loading developer service routes.")
        app.get("/api/dev/html", { ctx -> ctx.html(testReplace()) },
            roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
        app.get("/api/dev/form-template", { ctx -> ctx.json(testFormTemplate()) },
            roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }

    fun testFormTemplate(): ParamFormTemplate {
        val col = DatabaseConnector.database.getCollection<ParamFormTemplate>("FORM_TEMPLATES")
        println(col.countDocuments())
        println(col.find().first())
        return col.find().first() ?: ParamFormTemplate("no_found", "Template Not Found")
    }

    fun testReplace(): String {
        var html = testHtml()
        html = html.replace("{{title}}", "Report title")
        return html
    }

    fun testHtml(): String {
        return buildString {
            appendln("<!DOCTYPE html>")
            appendHTML().html {
                head {
                    appendln("\n" +
                            "<link rel=\"stylesheet\" href=\"https://unpkg.com/@clr/ui/clr-ui.min.css\" />")
                }
                body {
                    h1 { +"{{title}}" }
                    a("http://kotlinlang.org") { +"link" }
                }
            }
            appendln()
        }
    }
}