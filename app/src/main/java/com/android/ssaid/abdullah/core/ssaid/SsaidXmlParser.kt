package com.android.ssaid.abdullah.core.ssaid

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

data class SsaidXmlEntry(
    val uid: String,
    val packageName: String,
    val ssaid: String
)

object SsaidXmlParser {

    fun parse(xmlContent: String): Map<String, SsaidXmlEntry> {
        val result = mutableMapOf<String, SsaidXmlEntry>()
        if (xmlContent.isBlank()) return result
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "setting") {
                    val uid = parser.getAttributeValue(null, "name").orEmpty()
                    val pkg = parser.getAttributeValue(null, "package").orEmpty()
                    val value = parser.getAttributeValue(null, "value").orEmpty()
                    if (pkg.isNotBlank() && value.isNotBlank() && uid != "userkey") {
                        result[pkg] = SsaidXmlEntry(uid = uid, packageName = pkg, ssaid = value)
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Throwable) {
            val tagRegex = Regex("<setting\\s+[^>]+/>")
            tagRegex.findAll(xmlContent).forEach { match ->
                val tag = match.value
                val pkg = extractAttr(tag, "package")
                val value = extractAttr(tag, "value")
                val uid = extractAttr(tag, "name")
                if (pkg.isNotBlank() && value.isNotBlank() && uid != "userkey") {
                    result[pkg] = SsaidXmlEntry(uid = uid, packageName = pkg, ssaid = value)
                }
            }
        }
        return result
    }

    fun replacePackageSsaid(xmlContent: String, targetPackage: String, newSsaid: String): String? {
        val normalized = SsaidGenerator.normalize(newSsaid)
        if (!SsaidGenerator.isValid(normalized)) return null

        val settingTagRegex = Regex("<setting\\s+[^>]*/>")
        var replaced = false

        val updated = settingTagRegex.replace(xmlContent) { match ->
            val tag = match.value
            val pkg = extractAttr(tag, "package")
            val name = extractAttr(tag, "name")
            if (pkg == targetPackage && name != "userkey") {
                replaced = true
                var modifiedTag = replaceAttr(tag, "value", normalized)
                if (modifiedTag.contains("defaultValue=\"")) {
                    modifiedTag = replaceAttr(modifiedTag, "defaultValue", normalized)
                }
                modifiedTag
            } else {
                tag
            }
        }

        return if (replaced) updated else null
    }

    private fun extractAttr(tag: String, attrName: String): String {
        val regex = Regex("\\b$attrName=\"([^\"]*)\"")
        return regex.find(tag)?.groupValues?.getOrNull(1).orEmpty()
    }

    private fun replaceAttr(tag: String, attrName: String, newValue: String): String {
        val regex = Regex("(\\b$attrName=\")([^\"]*)(\")")
        return regex.replace(tag) { mr ->
            "${mr.groupValues[1]}$newValue${mr.groupValues[3]}"
        }
    }
}
