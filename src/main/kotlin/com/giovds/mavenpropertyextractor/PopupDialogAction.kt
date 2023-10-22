package com.giovds.mavenpropertyextractor

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class PopupDialogAction : AnAction() {

    private var service: PropertyExtractorService = PropertyExtractorService()

    override fun actionPerformed(event: AnActionEvent) {
        val editor: Editor = event.getData(CommonDataKeys.EDITOR) ?: throw RuntimeException("Failed to get editor.")
        val file: PsiFile = event.getData(CommonDataKeys.PSI_FILE) ?: throw RuntimeException("File not found")
        service.extractProperty(editor.project!!, editor, file)
    }

    override fun update(event: AnActionEvent) {
        if (isDialogInEditorView()) {
            val file: PsiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return
            event.presentation.isEnabledAndVisible = service.isPomFile(file)
        }
    }

    private fun isDialogInEditorView(): Boolean {
        return ActionPlaces.isPopupPlace(ActionPlaces.EDITOR_POPUP)
    }
}