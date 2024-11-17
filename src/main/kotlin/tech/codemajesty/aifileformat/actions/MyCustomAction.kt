// MyCustomAction.kt
package tech.codemajesty.aifileformat.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import tech.codemajesty.aifileformat.ContentGenerator
import tech.codemajesty.aifileformat.dialog.OutputFormat
import tech.codemajesty.aifileformat.dialog.RemoveCommentsDialog

class MyCustomAction : AnAction() {
    // Add this method to specify the update thread
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val selectedItems = getSelectedFiles(e)

        if (project != null && selectedItems.isNotEmpty()) {
            val dialog = RemoveCommentsDialog(project, selectedItems)

            if (dialog.showAndGet()) {
                val removeComments = dialog.isRemoveCommentsSelected()
                val filesToProcess = dialog.getSelectedFiles()
                val format = dialog.getSelectedFormat()
                generateOutputFile(
                    project,
                    filesToProcess,
                    removeComments,
                    dialog.getProjectBasePath(),
                    format
                )
            }
        }
    }

    private fun generateOutputFile(
        project: Project,
        files: List<VirtualFile>,
        removeComments: Boolean,
        projectBasePath: String?,
        format: OutputFormat
    ) {
        val generator = ContentGenerator()
        val (content, extension) = generator.generateContent(
            files,
            removeComments,
            projectBasePath,
            format,
            this::getRelativePath,
            this::removeComments
        )
        createNewFile(project, content, extension)
    }

    private fun removeComments(content: String, fileExtension: String?): String {
        return when (fileExtension?.lowercase()) {
            "java", "kt", "kotlin", "js", "ts", "tsx", "cpp", "c", "cs" -> {
                content.replace(Regex("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/"), "")
                    .replace(Regex("//.*"), "")
                    .replace(Regex("(?m)^[ \t]*\r?\n"), "")
            }

            "py" -> {
                content.replace(Regex("\"\"\"[^\"]*\"\"\""), "")
                    .replace(Regex("#.*"), "")
                    .replace(Regex("(?m)^[ \t]*\r?\n"), "")
            }

            "html", "xml" -> {
                content.replace(Regex("<!--[^>]*-->"), "")
                    .replace(Regex("(?m)^[ \t]*\r?\n"), "")
            }

            else -> content
        }
    }

    private fun getRelativePath(file: VirtualFile, projectBasePath: String?): String {
        return projectBasePath?.let { basePath ->
            file.path.removePrefix(basePath).removePrefix("/")
        } ?: file.path
    }

    private fun createNewFile(project: Project, content: String, extension: String) {
        val virtualFile = LightVirtualFile("generated-files-${System.currentTimeMillis()}.$extension", content)
        WriteCommandAction.runWriteCommandAction(project) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
        }
    }

    private fun getSelectedFiles(e: AnActionEvent): List<VirtualFile> {
        return e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.toList() ?: emptyList()
    }

    override fun update(e: AnActionEvent) {
        val files = getSelectedFiles(e)
        e.presentation.isEnabledAndVisible = files.isNotEmpty()
    }
}