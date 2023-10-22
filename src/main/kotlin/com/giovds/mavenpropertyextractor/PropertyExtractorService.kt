package com.giovds.mavenpropertyextractor

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.castSafelyTo

class PropertyExtractorService {

    fun isPomFile(file: PsiFile): Boolean {
        return file is XmlFile && file.name == "pom.xml"
    }

    fun extractProperty(project: Project, editor: Editor, file: PsiFile) {
        if (!isPomFile(file)) return

        val primaryCaret: Caret = editor.caretModel.primaryCaret
        val primaryCaretOffset: Int = primaryCaret.offset

        val surroundingXmlTags = ArrayDeque<XmlTag>()
        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is XmlTag) {
                    val xmlTag: XmlTag = element
                    val startOffset: Int = xmlTag.textRange.startOffset
                    val endOffset: Int = xmlTag.textRange.endOffset

                    if (primaryCaretOffset in startOffset..endOffset) {
                        surroundingXmlTags.addFirst(xmlTag)
                    }
                }
                super.visitElement(element)
            }
        })

        if (surroundingXmlTags.isEmpty()) {
            throw RuntimeException("No surrounding XML element was found");
        }

        var versionNameTag: String? = null
        val innerXmlTag: XmlTag = surroundingXmlTags.first()
        if (innerXmlTag.text.startsWith("<version>") && innerXmlTag.text.endsWith("</version>")) {
            versionNameTag = innerXmlTag.parentTag!!.findFirstSubTag("artifactId")!!.value.text
        }
        var initialInputValue: String = innerXmlTag.value.text
        if (versionNameTag != null) {
            initialInputValue = versionNameTag.plus(".version")
        }
        val propertyName: String = getUserInputForPropertyName(project, initialInputValue, innerXmlTag) ?: return
        val projectRoot: XmlTag = file.castSafelyTo<XmlFile>()!!.rootTag!!

        WriteCommandAction.runWriteCommandAction(project) {
            innerXmlTag.value.text = String.format("\${%s}", propertyName);

            val propertiesElementFound: Boolean = projectRoot.findFirstSubTag("properties") != null
            val propertiesElement: XmlTag = projectRoot.findFirstSubTag("properties")
                ?: projectRoot.createChildTag("properties", null, null, true)
            val newProperty: XmlTag = propertiesElement.createChildTag(propertyName, null, initialInputValue, true)
            propertiesElement.addSubTag(newProperty, false)

            if (!propertiesElementFound) {
                projectRoot.addSubTag(propertiesElement, false)
            }

            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
        }
    }

    private fun getUserInputForPropertyName(project: Project, initialValue: String, tag: XmlTag): String? {
        return Messages.showInputDialog(
            project, "Enter a property name", "Name Property",
            Messages.getQuestionIcon(), initialValue, null, tag.value.textRange
        )
    }
}