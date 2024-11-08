package tech.codemajesty.aifileformat

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import tech.codemajesty.aifileformat.dialog.OutputFormat

class ContentGenerator {
    fun generateContent(
        files: List<VirtualFile>,
        removeComments: Boolean,
        projectBasePath: String?,
        format: OutputFormat,
        getRelativePath: (VirtualFile, String?) -> String,
        removeCommentsFunction: (String, String?) -> String
    ): Pair<String, String> {
        return when (format) {
            OutputFormat.MARKDOWN -> generateMarkdown(files, removeComments, projectBasePath, getRelativePath, removeCommentsFunction)
            OutputFormat.HTML -> generateHtml(files, removeComments, projectBasePath, getRelativePath, removeCommentsFunction)
            OutputFormat.PLAIN_TEXT -> generatePlainText(files, removeComments, projectBasePath, getRelativePath, removeCommentsFunction)
        }
    }

    private fun generateMarkdown(
        files: List<VirtualFile>,
        removeComments: Boolean,
        projectBasePath: String?,
        getRelativePath: (VirtualFile, String?) -> String,
        removeCommentsFunc: (String, String?) -> String
    ): Pair<String, String> {
        val sb = StringBuilder()
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        sb.appendLine("This file is a merged representation of the selected files.")
        sb.appendLine("Generated on: $currentTime")
        sb.appendLine()

        if (removeComments) {
            sb.appendLine("Note: Comments have been removed from the files.")
            sb.appendLine()
        }

        sb.appendLine("# File Summary")
        sb.appendLine()
        sb.appendLine("## Repository Structure")
        sb.appendLine("```")
        files.forEach { file ->
            sb.appendLine(getRelativePath(file, projectBasePath))
        }
        sb.appendLine("```")
        sb.appendLine()

        sb.appendLine("# Repository Files")
        sb.appendLine()

        files.forEach { file ->
            sb.appendLine("## File: ${getRelativePath(file, projectBasePath)}")
            sb.appendLine("```${file.extension}")
            var content = FileDocumentManager.getInstance().getDocument(file)?.text ?: ""
            if (removeComments) {
                content = removeCommentsFunc(content, file.extension)
            }
            sb.appendLine(content)
            sb.appendLine("```")
            sb.appendLine()
        }

        return Pair(sb.toString(), "md")
    }

    private fun generateHtml(
        files: List<VirtualFile>,
        removeComments: Boolean,
        projectBasePath: String?,
        getRelativePath: (VirtualFile, String?) -> String,
        removeCommentsFunc: (String, String?) -> String
    ): Pair<String, String> {
        val sb = StringBuilder()
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        sb.appendLine("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Generated Files</title>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/default.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
                <script>hljs.highlightAll();</script>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    pre { background: #f5f5f5; padding: 15px; border-radius: 5px; }
                    .file-name { color: #2c3e50; font-size: 1.2em; margin-top: 20px; }
                    .file-content { margin-bottom: 30px; }
                </style>
            </head>
            <body>
        """.trimIndent())

        sb.appendLine("<h1>Generated Files</h1>")
        sb.appendLine("<p>Generated on: $currentTime</p>")

        if (removeComments) {
            sb.appendLine("<p><em>Note: Comments have been removed from the files.</em></p>")
        }

        sb.appendLine("<h2>File Structure</h2>")
        sb.appendLine("<pre>")
        files.forEach { file ->
            sb.appendLine(getRelativePath(file, projectBasePath))
        }
        sb.appendLine("</pre>")

        sb.appendLine("<h2>File Contents</h2>")
        files.forEach { file ->
            sb.appendLine("<div class='file-name'>File: ${getRelativePath(file, projectBasePath)}</div>")
            var content = FileDocumentManager.getInstance().getDocument(file)?.text ?: ""
            if (removeComments) {
                content = removeCommentsFunc(content, file.extension)
            }
            sb.appendLine("<pre><code class='language-${file.extension}'>")
            sb.appendLine(content.replace("<", "&lt;").replace(">", "&gt;"))
            sb.appendLine("</code></pre>")
        }

        sb.appendLine("</body></html>")
        return Pair(sb.toString(), "html")
    }

    private fun generatePlainText(
        files: List<VirtualFile>,
        removeComments: Boolean,
        projectBasePath: String?,
        getRelativePath: (VirtualFile, String?) -> String,
        removeCommentsFunc: (String, String?) -> String
    ): Pair<String, String> {
        val sb = StringBuilder()
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        sb.appendLine("Generated Files")
        sb.appendLine("==============")
        sb.appendLine()
        sb.appendLine("Generated on: $currentTime")
        sb.appendLine()

        if (removeComments) {
            sb.appendLine("Note: Comments have been removed from the files.")
            sb.appendLine()
        }

        sb.appendLine("File Structure:")
        sb.appendLine("---------------")
        files.forEach { file ->
            sb.appendLine(getRelativePath(file, projectBasePath))
        }
        sb.appendLine()

        sb.appendLine("File Contents:")
        sb.appendLine("--------------")
        files.forEach { file ->
            sb.appendLine("==> ${getRelativePath(file, projectBasePath)} <==")
            var content = FileDocumentManager.getInstance().getDocument(file)?.text ?: ""
            if (removeComments) {
                content = removeCommentsFunc(content, file.extension)
            }
            sb.appendLine(content)
            sb.appendLine()
            sb.appendLine("-".repeat(80))
            sb.appendLine()
        }

        return Pair(sb.toString(), "txt")
    }
}