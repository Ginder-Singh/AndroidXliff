import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Error: Invalid arguments provided.")
        return
    }
    val toXliff = args[0] == "toXliff"
    val directory = File(args[1])
    if (directory.isDirectory.not()) {
        println("Error: Provide android resource directory path.")
        return
    }
    if (toXliff) {
        val resourceDirectories = directory.listFiles()?.filter { it.name.startsWith("values-") } ?: emptyList()
        if (resourceDirectories.isEmpty()) {
            println("Error: Check resource have correct files present.")
            return
        }
        val supportedLanguages = args[2].split(",")
        createXliffFiles(directory, supportedLanguages)
    } else {
        val xlfFiles = directory.listFiles()?.toList()
        if (xlfFiles == null) {
            println("Error: No files found in xlf directory.")
            return
        }
        val xmlDir = File(args[2])
        if (xmlDir.exists().not()) {
            println("Error: Xml does not exists.")
            return
        }
        createXmlFiles(xlfFiles, xmlDir)
    }
}

fun createXmlFiles(xlfFiles: List<File>, xmlDir: File) {
    xlfFiles.forEach {
        val document = parseAndroidXml(it)
        val targetLang =
            document?.getElementsByTagName("file")?.item(0)?.attributes?.getNamedItem("target-language")?.nodeValue
                ?: "en"
        val targetTags = mutableListOf<Node>()
        val targets = document?.getElementsByTagName("target")
        if (targets != null) {
            for (i in 0 until targets.length) {
                targetTags.add(targets.item(i))
            }
        }
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<resources>\n")
        val srcTags = document?.getElementsByTagName("trans-unit")
        if (srcTags != null) {
            for (i in 0 until srcTags.length) {
                val node = srcTags.item(i)
                val id = node.attributes.getNamedItem("id").nodeValue
                targetTags.firstOrNull { targetNode ->
                    targetNode.parentNode.isSameNode(node)
                }?.let { targetNode ->
                    sb.append(" <string name=\"$id\">${targetNode.textContent}</string>\n")
                }
            }
        }
        sb.append("</resources>\n")
        val dir = File(xmlDir, "values-$targetLang")
        if (dir.exists().not()) {
            dir.mkdirs()
        }
        val file = File(dir, "strings.xml")
        if (file.exists().not()) {
            file.createNewFile()
        }
        file.writeText(sb.toString())
    }
    println("Successfully saved xml files.")
}

fun createXliffFiles(resourcePath: File, supportedLanguages: List<String>) {
    val sourceFile = File(resourcePath, "values/strings.xml")
    val srcDocument = parseAndroidXml(sourceFile)
    val srcTags = srcDocument?.getElementsByTagName("string")
    val srcNodes = mutableListOf<Node>()
    if (srcTags != null) {
        for (i in 0 until srcTags.length) {
            srcNodes.add(srcTags.item(i))
        }
    }
    supportedLanguages.map { targetLang ->
        val targetFile = File(resourcePath, "values-$targetLang/strings.xml")
        val sourceLang = "en"
        return@map AndroidResource(targetLang, sourceLang, targetFile, srcNodes)
    }.map {
        val targetDocument = parseAndroidXml(it.targetFile)
        val xliff = buildXliff(it.sourceNodes, targetDocument, it.sourceLanguage, it.targetLanguage)
        val xlfDir = File(resourcePath, "xlf")
        if (xlfDir.exists().not()) {
            xlfDir.mkdirs()
        }
        val xlfFile = File(xlfDir, "${it.targetLanguage}.xlf")
        if (xlfFile.exists().not()) {
            xlfFile.createNewFile()
        }
        xlfFile.writeText(xliff.content)
        return@map "${it.targetLanguage} Done"
    }.joinToString(separator = ", ", prefix = "[", postfix = "]").let {
        println("Successfully saved xliff files.")
    }
}

fun parseAndroidXml(srcFile: File): Document? {
    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val result = runCatching { builder.parse(srcFile) }
    return if (result.isFailure) {
        null
    } else {
        val document = result.getOrThrow()
        document.normalizeDocument()
        document
    }
}

fun buildXliff(sourceNodes: MutableList<Node>, targetDocument: Document?, srcLang: String, targetLang: String): Xliff {
    val targetTags = targetDocument?.getElementsByTagName("string")
    val targetNodes = mutableListOf<Node>()
    if (targetTags != null) {
        for (i in 0 until targetTags.length) {
            targetNodes.add(targetTags.item(i))
        }
    }
    val xliff = Xliff("1.2")
    xliff.files = sourceNodes.map {
        val key = it.attributes.getNamedItem("name").nodeValue
        val srcValue = it.textContent
        val targetValue =
            targetNodes.firstOrNull { srcNode -> srcNode.attributes.getNamedItem("name").nodeValue == key }?.textContent?.replace(
                "&",
                "&amp;"
            )
        val file = XFile(key, "plaintext", srcLang, targetLang)
        file.body = XBody(listOf(XUnit(key, srcValue.replace("&", "&amp;"), targetValue)))
        return@map file
    }
    return xliff
}