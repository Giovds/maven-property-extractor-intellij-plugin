package com.giovds.mavenpropertyextractor

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class PropertyExtractorIntentionAction : IntentionAction, PriorityAction {

    private var service: PropertyExtractorService = PropertyExtractorService()

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun getText(): String {
        return "Extract Maven property"
    }

    override fun getFamilyName(): String {
        return "Extract Maven property"
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return file != null && service.isPomFile(file)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return

        service.extractProperty(project, editor, file)
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.HIGH
    }
}