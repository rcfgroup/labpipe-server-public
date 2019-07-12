package uk.ac.le.ember.labpipe.server.notification

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import j2html.TagCreator
import j2html.TagCreator.*
import j2html.tags.ContainerTag
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import uk.ac.le.ember.labpipe.server.data.FormTemplate
import uk.ac.le.ember.labpipe.server.data.Operator
import uk.ac.le.ember.labpipe.server.sessions.*

data class ReportTemplate(var code: String) {
    var name: String = ""

    @JsonProperty("form_code")
    var formCode: String = ""

    @JsonProperty("text_template")
    var textTemplate: String = ""
    @JsonProperty("html_template")
    var htmlTemplate: MutableList<Element> = mutableListOf()
    var active: Boolean = false
}

data class Element(var type: String) {
    var source: String = ""
    var method: String = ""
    var value: String = ""
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
    PROPERTY_DIRECT("PROPERTY_DIRECT"),
    PROPERTY_NESTED("PROPERTY_NESTED"),
    SELECT("SELECT")
}

enum class ElementSource(val value: String) {
    STATIC("STATIC"),
    FORM_DATA("FORM_DATA"),
    FORM_TEMPLATE("FORM_CONFIG")
}

object ReportUtil {
    fun generateHtml(operator: Operator, form: FormTemplate, formData: JsonObject): String? {
        val reportTemplate = Runtime.mongoDatabase.getCollection<ReportTemplate>(RequiredMongoDBCollections.REPORT_TEMPLATES.value)
            .findOne (ReportTemplate::formCode eq form.code, ReportTemplate::active eq true )
        reportTemplate?.run {
            Runtime.logger.info { "Report template found." }
            Runtime.logger.info { reportTemplate.htmlTemplate.size }
            val elements = reportTemplate.htmlTemplate
            elements.sortedWith(compareBy { it.order })
            var reportBody = body()
            elements.forEach {element ->
                run {
                    Runtime.logger.info { "Processing report element: $element" }
                    when (ElementType.values().find { it.value.equals(element.type, true) }) {
                        ElementType.REPORT_TITLE -> {
                            when (ElementSource.values().find { it.value.equals(element.source, true) }) {
                                ElementSource.STATIC -> {
                                    when (ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.VALUE -> reportBody.with(h1(element.value))
                                        else -> reportBody.with(
                                            div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Static source only supports string value at the moment.")
                                            )
                                        )
                                    }
                                }
                                ElementSource.FORM_DATA -> {
                                    when (val method = ElementMethod.values().find { it.value.equals(element.method, true) }) {
                                        ElementMethod.PROPERTY_DIRECT -> {
                                            reportBody.with(h1(formData.get(element.value).asString))
                                        }
                                        ElementMethod.PROPERTY_NESTED -> {
                                            val data = getNestedFormData(formData, element.value)
                                            if (data != null) {
                                                reportBody.with(h1(data.asString))
                                            } else {
                                                reportBody.with(div().withClasses("alert", "alert-danger").with(
                                                    h5("ERROR").withClasses("alert-heading"),
                                                    p("Form data does not have property [${element.value}]")
                                                ))
                                            }
                                        }
                                        ElementMethod.SELECT -> {
                                            // TODO
                                            reportBody.with(div().withClasses("alert", "alert-danger").with(
                                                h5("ERROR").withClasses("alert-heading"),
                                                p("Element source [${ElementSource.FORM_DATA.value}] does not support access method [${method?.value}]")
                                            ))
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

                                }
                            }
                            val value: String = when (valueArray[0]) {
                                ElementValueSource.STATIC.getValueSourceName() -> valueArray[1]
                                ElementValueSource.FORM_DATA.getValueSourceName() -> ReportUtil.getFormDataAsString(
                                    formData,
                                    valueArray[1]
                                )
                                ElementValueSource.FORM_TEMPLATE.getValueSourceName() -> form.getProperty(valueArray[1])
                                    ?: "Unable to get ${valueArray[1]} from form template"
                                else -> "Invalid format in report config"
                            }
                            println("H1: $value")
                            reportBody.with(TagCreator.h1(value))
                        }
                        ElementType.SECTION_TITLE.value -> {
                            val valueArray = it.value.split("::")
                            val value: String = when (valueArray[0]) {
                                ElementValueSource.STATIC.getValueSourceName() -> valueArray[1]
                                ElementValueSource.FORM_DATA.getValueSourceName() -> ReportUtil.getFormDataAsString(
                                    formData,
                                    valueArray[1]
                                )
                                ElementValueSource.FORM_TEMPLATE.getValueSourceName() -> form.getProperty(valueArray[1])
                                    ?: "Unable to get valueArray[1] from form template"
                                else -> "Invalid format in report config"
                            }
                            println("H3: $value")
                            reportBody.with(TagCreator.h3(value))
                        }
                        ElementType.TABLE_KEYVALUE.value -> {
                            val valueArray = it.value.split("::")
                            val value: JsonObject? = when (valueArray[0]) {
                                ElementValueSource.FORM_DATA.getValueSourceName() -> formData.getAsJsonObject(valueArray[1])
                                else -> null
                            }
                            var table = TagCreator.table().withClasses("table", "table-responsive")
                            value?.run {
                                val entries = value.entrySet()
                                val chunkedEntries = entries.chunked(4)

                                chunkedEntries.forEach { chunk ->
                                    var row = TagCreator.tr()
                                    chunk.forEach {
                                        row.with(
                                            TagCreator.th(it.key), TagCreator.td(it.value.asString)
                                        )
                                    }
                                    table.with(row)
                                }
                            }
                            reportBody.with(table)
                        }
                        ElementType.TABLE_LOOP.value -> {
                            println("To be implemented")
                            // TODO
                        }
                        ElementType.LIST.value -> {
                            val valueArray = it.value.split("::")
                            val value: JsonArray? = when (valueArray[0]) {
                                ElementValueSource.FORM_DATA.getValueSourceName() -> ReportUtil.getFormDataAsArray(
                                    formData,
                                    valueArray[1]
                                )
                                else -> null
                            }
                            var list = TagCreator.ol()
                            value?.run {
                                value.forEach { list.with(TagCreator.li(it.asString)) }
                            }
                            reportBody.with(list)
                        }
                        else -> reportBody.with(TagCreator.p("Invalid format in report config"))
                    }
                }
            }
            val report = TagCreator.html(
                TagCreator.head(
                    TagCreator.link().withRel("stylesheet").withHref("https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css")
                ),
                reportBody
            )
            val result = report.renderFormatted()
            println(result)
            return result
        }
        return null
    }

    fun handleElement(element: Element) {
        when (element.type) {
            ElementType.REPORT_TITLE ->
        }
    }

    fun handleElementValue(valueString: String): ContainerTag? {
        if (!Statics.REPORT_ELEMENT_VALUE_REGEX.matches(valueString)) {
            return p("[ERROR] Invalid value source in report template element.")
        }
        val matchResult = Statics.REPORT_ELEMENT_VALUE_REGEX.matchEntire(valueString)
        matchResult?.run {
            val (source, method, value) = matchResult.destructured
            when (source) {
                ElementValueSource.STATIC.getValueSourceName() -> {
                    when(method) {
                        ElementStaticAccessMethod.VALUE ->
                    }
                }
                ElementValueSource.FORM_DATA.getValueSourceName() -> {}
                ElementValueSource.FORM_TEMPLATE.getValueSourceName() -> {}
                else -> return p("[ERROR] Invalid value type in report template element.")
            }
        }
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

    fun getNestedFormData(formData: JsonObject, keyString: String): JsonElement? {
        return if (keyString.contains('.')) {
            val keyArray = keyString.split(".")
            var result: JsonElement? = null
            keyArray.forEach {
                run {
                    result = formData.get(it)
                }
            }
            result
        } else {
            formData.get(keyString)
        }
    }
}