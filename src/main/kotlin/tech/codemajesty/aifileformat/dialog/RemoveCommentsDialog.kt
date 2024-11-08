package tech.codemajesty.aifileformat.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

enum class OutputFormat {
    MARKDOWN,
    HTML,
    PLAIN_TEXT;

    override fun toString(): String {
        return when (this) {
            MARKDOWN -> "Markdown"
            HTML -> "HTML"
            PLAIN_TEXT -> "Plain Text"
        }
    }
}

class RemoveCommentsDialog(
    project: Project?,
    selectedFiles: List<VirtualFile>
) : DialogWrapper(project) {

    private val removeCommentsCheckBox = JCheckBox("Remove Comments")
    private val formatComboBox = JComboBox(OutputFormat.values())
    private val allFiles = getAllFilesRecursively(selectedFiles)
    private val projectBasePath = project?.basePath

    init {
        title = "Generate AI Content"
        init()
    }

    private fun getAllFilesRecursively(items: List<VirtualFile>): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()

        fun processItem(file: VirtualFile) {
            if (file.isDirectory) {
                file.children.forEach { processItem(it) }
            } else {
                result.add(file)
            }
        }

        items.forEach { processItem(it) }
        return result
    }

    private fun getRelativePath(file: VirtualFile): String {
        return projectBasePath?.let { basePath ->
            file.path.removePrefix(basePath).removePrefix("/")
        } ?: file.path
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())

        // Create options panel for checkbox and format selector
        val optionsPanel = JPanel(FlowLayout(FlowLayout.LEFT))

        // Add checkbox
        optionsPanel.add(removeCommentsCheckBox)

        // Add format selector with label
        optionsPanel.add(JLabel("Output Format:"))
        optionsPanel.add(formatComboBox)

        panel.add(optionsPanel, BorderLayout.NORTH)

        // Add files list with count
        val fileListModel = DefaultListModel<String>().apply {
            addElement("Total files found: ${allFiles.size}")
            addElement("") // Empty line for spacing
            allFiles.forEach { addElement(getRelativePath(it)) }
        }

        val filesList = JList(fileListModel)
        val scrollPane = JScrollPane(filesList)
        scrollPane.preferredSize = Dimension(500, 300)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    fun isRemoveCommentsSelected(): Boolean = removeCommentsCheckBox.isSelected
    fun getSelectedFiles(): List<VirtualFile> = allFiles
    fun getProjectBasePath(): String? = projectBasePath
    fun getSelectedFormat(): OutputFormat = formatComboBox.selectedItem as OutputFormat
}