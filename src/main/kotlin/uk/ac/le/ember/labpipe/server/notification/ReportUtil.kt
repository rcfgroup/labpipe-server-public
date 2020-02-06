package uk.ac.le.ember.labpipe.server.notification

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import j2html.TagCreator
import j2html.TagCreator.*
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import uk.ac.le.ember.labpipe.server.FormTemplate
import uk.ac.le.ember.labpipe.server.MONGO
import uk.ac.le.ember.labpipe.server.Operator
import uk.ac.le.ember.labpipe.server.sessions.Runtime

data class ReportTemplate(var identifier: String) {
    var name: String = ""
    var formIdentifier: String = ""

    var text: String = ""
    var html: MutableList<Element> = mutableListOf()
    var active: Boolean = false
}

data class Element(var type: String) {
    var source: String = ""
    var method: String = ""
    var value: MutableList<String> = mutableListOf()
    var order: Int = 1
}

enum class ElementType(val value: String) {
    REPORT_TITLE("REPORT_TITLE"),
    SECTION_TITLE("SECTION_TITLE"),
    TEXT("TEXT"),
    SUB_TEXT("SUB_TEXT"),
    TABLE_KEYVALUE("TABLE_KEYVALUE"),
    TABLE_LOOP("TABLE_LOOP"),
    LIST("LIST")
}

enum class ElementMethod(val value: String) {
    VALUE("VALUE"),
    JSON("JSON"),
    PROPERTY("PROPERTY"),
    SELECT("SELECT")
}

enum class ElementSource(val value: String) {
    STATIC("STATIC"),
    FORM_DATA("FORM_DATA"),
    FORM_TEMPLATE("FORM_CONFIG")
}

object ReportUtil {
    fun generateHtml(operator: Operator, form: FormTemplate, formData: JsonObject): String? {
        val reportTemplate = MONGO.COLLECTIONS.REPORT_TEMPLATES.findOne(ReportTemplate::formIdentifier eq form.identifier, ReportTemplate::active eq true)
        reportTemplate?.run {
            Runtime.logger.info { "Report template found." }
            Runtime.logger.info { reportTemplate.html.size }
            val elements = reportTemplate.html
            elements.sortedWith(compareBy { it.order })
            var reportBody = body()
            reportBody.with(
                header().withClasses("header", "header-5").with(
                    div().withClasses("branding").with(
                        span("LabPipe Notification").withClasses("title")
                    )
                )
            )
            elements.forEach { element ->
                run {
                    Runtime.logger.info { "Processing report element: $element" }
                    when (ElementType.values().find { it.value.equals(element.type, true) }) {
                        ElementType.REPORT_TITLE -> {
                            when (ElementSource.values().find { it.value.equals(element.source, true) }) {
                                ElementSource.STATIC -> {
                                    when (ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.VALUE -> reportBody.with(h1(element.value[0]))
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Static source only supports string value at the moment.")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_DATA -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            val data = getNestedFormData(formData, element.value[0])
                                            if (data != null) {
                                                reportBody.with(h1(data.asString))
                                            } else {
                                                reportBody.with(
                                                    div().withClasses("alert", "alert-danger").with(
                                                        h5("ERROR").withClasses("alert-heading"),
                                                        p("Form data does not have property [${element.value}]")
                                                    )
                                                )
                                            }
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO add support to loop through value array
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_TEMPLATE -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            reportBody.with(h1(getNestedFormData(formData, element.value[0])?.asString))
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                else -> reportBody.with(
                                    div().withClasses("alert", "alert-danger").with(
                                        h5("ERROR").withClasses("alert-heading"),
                                        p("Element source not supported.")
                                    )
                                )
                            }
                        }
                        ElementType.SECTION_TITLE -> {
                            when (ElementSource.values().find { it.value.equals(element.source, true) }) {
                                ElementSource.STATIC -> {
                                    when (ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.VALUE -> reportBody.with(h3(element.value[0]))
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Static source only supports string value at the moment.")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_DATA -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            val data = getNestedFormData(formData, element.value[0])
                                            if (data != null) {
                                                reportBody.with(h3(data.asString))
                                            } else {
                                                reportBody.with(
                                                    div().withClasses("alert", "alert-danger").with(
                                                        h5("ERROR").withClasses("alert-heading"),
                                                        p("Form data does not have property [${element.value}]")
                                                    )
                                                )
                                            }
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_TEMPLATE -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            //TODO add support for accessing form template nested properties from key array
                                            reportBody.with(h3(form.getProperty(element.value[0])))
                                        }
                                        ElementMethod.SELECT -> {
                                            //TODO add support for accessing form template properties from key array
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                else -> reportBody.with(
                                    div().withClasses("alert", "alert-danger").with(
                                        h5("ERROR").withClasses("alert-heading"),
                                        p("Element source not supported.")
                                    )
                                )
                            }
                        }
                        ElementType.TABLE_KEYVALUE -> {
                            when (ElementSource.values().find { it.value.equals(element.source, true) }) {
                                ElementSource.STATIC -> {
                                    when (ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.VALUE -> reportBody.with(table(tr(td(element.value[0]))))
                                        ElementMethod.JSON -> {
                                            val value = JsonParser.parseString(element.value[0]).asJsonObject
                                            var table = table().withClasses("table", "table-responsive")
                                            value?.run {
                                                val entries = value.entrySet()
                                                val chunkedEntries = entries.chunked(4)

                                                chunkedEntries.forEach { chunk ->
                                                    var row = tr()
                                                    chunk.forEach {
                                                        row.with(
                                                            th(it.key), td(it.value.asString)
                                                        )
                                                    }
                                                    table.with(row)
                                                }
                                            }
                                            reportBody.with(table)
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Static source does not support [Access Method: ${element.method}] at the moment.")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_DATA -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            val propertyData = getNestedFormData(formData, element.value[0])
                                            if (propertyData != null) {
                                                if (propertyData.isJsonObject) {
                                                    val value = propertyData.asJsonObject
                                                    var table = table().withClasses("table", "table-responsive")
                                                    value?.run {
                                                        val entries = value.entrySet()
                                                        val chunkedEntries = entries.chunked(4)
                                                        chunkedEntries.forEach { chunk ->
                                                            var row = TagCreator.tr()
                                                            chunk.forEach {
                                                                row.with(th(it.key), td(it.value.asString))
                                                            }
                                                            table.with(row)
                                                        }
                                                    }
                                                    reportBody.with(table)
                                                } else {
                                                    reportBody.with(table(tr(td(propertyData.asString))))
                                                }
                                            } else {
                                                reportBody.with(
                                                    div().withClasses("alert", "alert-danger").with(
                                                        h5("ERROR").withClasses("alert-heading"),
                                                        p("Form data does not have property [${element.value}]")
                                                    )
                                                )
                                            }
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_TEMPLATE -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                else -> reportBody.with(
                                    div().withClasses("alert", "alert-danger").with(
                                        h5("ERROR").withClasses("alert-heading"),
                                        p("Element source not supported.")
                                    )
                                )
                            }
                        }
                        ElementType.TABLE_LOOP -> {
                            println("To be implemented")
                            // TODO
                        }
                        ElementType.LIST -> {
                            when (ElementSource.values().find { it.value.equals(element.source, true) }) {
                                ElementSource.STATIC -> {
                                    when (ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.VALUE -> reportBody.with(table(tr(td(element.value[0]))))
                                        ElementMethod.JSON -> {
                                            val value = JsonParser.parseString(element.value[0]).asJsonObject
                                            var table = table().withClasses("table", "table-responsive")
                                            value?.run {
                                                val entries = value.entrySet()
                                                val chunkedEntries = entries.chunked(4)

                                                chunkedEntries.forEach { chunk ->
                                                    var row = tr()
                                                    chunk.forEach {
                                                        row.with(
                                                            th(it.key), td(it.value.asString)
                                                        )
                                                    }
                                                    table.with(row)
                                                }
                                            }
                                            reportBody.with(table)
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Static source does not support [Access Method: ${element.method}] at the moment.")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_DATA -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            val propertyData = getNestedFormData(formData, element.value[0])
                                            if (propertyData != null) {
                                                if (propertyData.isJsonArray) {
                                                    val value = propertyData.asJsonArray
                                                    var list = ol()
                                                    value?.run {
                                                        value.forEach { list.with(li(it.asString)) }
                                                    }
                                                    reportBody.with(list)
                                                } else {
                                                    reportBody.with(
                                                        div().withClasses("alert", "alert-danger").with(
                                                            h5("ERROR").withClasses("alert-heading"),
                                                            p("Form data does not have property [${element.value}]")
                                                        )
                                                    )
                                                }
                                            } else {
                                                reportBody.with(
                                                    div().withClasses("alert", "alert-danger").with(
                                                        h5("ERROR").withClasses("alert-heading"),
                                                        p("Form data does not have property [${element.value}]")
                                                    )
                                                )
                                            }
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_TEMPLATE -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                else -> reportBody.with(
                                    div().withClasses("alert", "alert-danger").with(
                                        h5("ERROR").withClasses("alert-heading"),
                                        p("Element source [${ElementSource.FORM_DATA.value}] is not supported.")
                                    )
                                )
                            }
                        }
                        ElementType.TEXT -> {
                            when (ElementSource.values().find { it.value.equals(element.source, true) }) {
                                ElementSource.STATIC -> {
                                    when (ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.VALUE -> reportBody.with(p(element.value[0]))
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Static source only supports string value at the moment.")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_DATA -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            val data = getNestedFormData(formData, element.value[0])
                                            if (data != null) {
                                                reportBody.with(p(data.asString))
                                            } else {
                                                reportBody.with(
                                                    div().withClasses("alert", "alert-danger").with(
                                                        h5("ERROR").withClasses("alert-heading"),
                                                        p("Form data does not have property [${element.value}]")
                                                    )
                                                )
                                            }
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO add support to loop through value array
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_TEMPLATE -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            reportBody.with(p(getNestedFormData(formData, element.value[0])?.asString))
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                else -> reportBody.with(
                                    div().withClasses("alert", "alert-danger").with(
                                        h5("ERROR").withClasses("alert-heading"),
                                        p("Element source not supported.")
                                    )
                                )
                            }
                        }
                        ElementType.SUB_TEXT -> {
                            when (ElementSource.values().find { it.value.equals(element.source, true) }) {
                                ElementSource.STATIC -> {
                                    when (ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.VALUE -> reportBody.with(p(element.value[0]).withClasses("text-muted"))
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Static source only supports string value at the moment.")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_DATA -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            val data = getNestedFormData(formData, element.value[0])
                                            if (data != null) {
                                                reportBody.with(p(data.asString).withClasses("text-muted"))
                                            } else {
                                                reportBody.with(
                                                    div().withClasses("alert", "alert-danger").with(
                                                        h5("ERROR").withClasses("alert-heading"),
                                                        p("Form data does not have property [${element.value}]")
                                                    )
                                                )
                                            }
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO add support to loop through value array
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_TEMPLATE -> {
                                    when (val method =
                                        ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY -> {
                                            reportBody.with(
                                                p(
                                                    getNestedFormData(
                                                        formData,
                                                        element.value[0]
                                                    )?.asString
                                                ).withClasses("text-muted")
                                            )
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(
                                                div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method.value}]")
                                                )
                                            )
                                        }
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            )
                                        )
                                    }
                                }
                                else -> reportBody.with(
                                    div().withClasses("alert", "alert-danger").with(
                                        h5("ERROR").withClasses("alert-heading"),
                                        p("Element source not supported.")
                                    )
                                )
                            }
                        }
                        else -> reportBody.with(
                            div().withClasses("alert", "alert-danger").with(
                                h5("ERROR").withClasses("alert-heading"),
                                p("Element type not supported.")
                            )
                        )
                    }
                }
            }
            val report = html(
                head(
                    TagCreator.link().withRel("stylesheet").withHref("https://unpkg.com/@clr/ui/clr-ui.min.css")
                ),
                reportBody
            )
            val result = report.renderFormatted()
            println(result)
            return result
        }
        return null
    }

    fun getFormDataAsString(formData: JsonObject, keyString: String): String {
        return if (keyString.contains('.')) {
            val keyArray = keyString.split(".")
            formData.getAsJsonObject(keyArray[0]).get(keyArray[1]).asString
        } else {
            formData.get(keyString).asString
        }
    }

    fun getFormDataAsArray(formData: JsonObject, keyString: String): JsonArray {
        return if (keyString.contains('.')) {
            val keyArray = keyString.split(".")
            formData.getAsJsonObject(keyArray[0]).getAsJsonArray(keyArray[1])
        } else {
            formData.getAsJsonArray(keyString)
        }
    }

    fun getNestedFormData(formData: JsonObject, keyString: String, inRecord: Boolean = true): JsonElement? {
        var key = if (inRecord) "record.$keyString" else keyString
        println(key)
        return if (key.contains('.')) {
            val keyArray = key.split(".")
            var result: JsonElement? = formData
            keyArray.forEach {
                    result = result?.asJsonObject?.get(it)
            }
            result
        } else {
            formData.get(key)
        }
    }
}