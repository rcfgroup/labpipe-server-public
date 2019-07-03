package uk.ac.le.ember.labpipe.server.services

import io.javalin.core.security.SecurityUtil.roles
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.auths.AuthManager
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.sessions.Runtime

object DevService {
    fun routes() {
        println("Loading developer service routes.")
        Runtime.server.get(
            "/api/dev/html", { ctx -> ctx.html(testReplace()) },
            roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
        Runtime.server.get(
            "/api/dev/form-template", { ctx -> ctx.json(testFormTemplate()) },
            roles(AuthManager.ApiRole.PUBLIC, AuthManager.ApiRole.AUTHORISED)
        )
    }

    fun testFormTemplate(): FormTemplate {
        val col = Runtime.mongoDatabase.getCollection<FormTemplate>("FORM_TEMPLATES")
        return col.find().first() ?: FormTemplate("no_found", "Template Not Found")
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
                    appendln(
                        "\n" +
                                "<link rel=\"stylesheet\" href=\"https://unpkg.com/@clr/ui/clr-ui.min.css\" />"
                    )
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