import org.w3c.dom.Node
import java.io.File
import java.util.*

data class Xliff(
    var version: String,
) {
    var files: List<XFile> = emptyList()

    val content: String
        get() {
            val sb = StringBuilder()
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            sb.append("<xliff xmlns=\"urn:oasis:names:tc:xliff:document:$version\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"$version\" xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:$version http://docs.oasis-open.org/xliff/v1.2/os/xliff-core-$version-strict.xsd\">\n")
            val firstFile = files[0]
            val uuid = UUID.randomUUID()
            sb.append(" <file original=\"$uuid\" datatype=\"plaintext\" source-language=\"${firstFile.sourceLanguage}\" target-language=\"${firstFile.targetLanguage}\">\n")
            sb.append("  <body>\n")
            for (file in files) {
                for (unit in file.body?.units!!) {
                    sb.append("  <trans-unit id=\"${unit.id}\" xml:space=\"preserve\">\n")
                    sb.append("    <source>${unit.source}</source>\n")
                    if (unit.target != null)
                    sb.append("    <target>${unit.target}</target>\n")
                    sb.append("  </trans-unit>\n")
                }
            }
            sb.append("  </body>\n")
            sb.append(" </file>\n")
            sb.append("</xliff>")
            return sb.toString()
        }
}

data class XFile(
    var original: String,
    var datatype: String,
    var sourceLanguage: String,
    var targetLanguage: String
) {
    var body: XBody? = null
}

data class XBody(var units: List<XUnit> = emptyList())

data class XUnit(
    var id: String,
    var source: String,
    var target: String?
)
data class AndroidResource(val targetLanguage: String, val sourceLanguage: String = "en", val targetFile: File, val sourceNodes: MutableList<Node>)