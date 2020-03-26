package org.jetbrains.kotlin.tools.projectWizard.wizard.ui.firstStep

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jetbrains.kotlin.tools.projectWizard.core.Context
import org.jetbrains.kotlin.tools.projectWizard.core.entity.settings.DropDownSettingType
import org.jetbrains.kotlin.tools.projectWizard.core.entity.settings.SettingReference
import org.jetbrains.kotlin.tools.projectWizard.core.entity.settings.reference
import org.jetbrains.kotlin.tools.projectWizard.plugins.projectTemplates.ProjectTemplatesPlugin
import org.jetbrains.kotlin.tools.projectWizard.plugins.projectTemplates.applyProjectTemplate
import org.jetbrains.kotlin.tools.projectWizard.projectTemplates.ProjectTemplate
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.*
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.setting.SettingComponent
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.setting.ValidationIndicator
import javax.swing.JComponent

class ProjectTemplateSettingComponent(
    context: Context
) : SettingComponent<ProjectTemplate, DropDownSettingType<ProjectTemplate>>(
    ProjectTemplatesPlugin::template.reference,
    context
) {
    override val validationIndicator: ValidationIndicator? get() = null
    override val needCentering: Boolean = false
    private val templateDescriptionComponent = TemplateDescriptionComponent().asSubComponent()

    private val list = ImmutableSingleSelectableListWithIcon(
        setting.type.values,
        renderValue = { value ->
            icon = value.projectKind.icon
            append(value.title)
        },
        onValueSelected = { value = it }
    ).addBorder(JBUI.Borders.customLine(JBColor.border()))


    override val component: JComponent = borderPanel {
        addToCenter(borderPanel { addToCenter(list) }.addBorder(JBUI.Borders.empty(0,/*left*/ 3, 0, /*right*/ 3)))
        addToBottom(templateDescriptionComponent.component.addBorder(JBUI.Borders.empty(/*top*/8,/*left*/ 3, /*bottom*/10, 0)))
    }

    private fun applySelectedTemplate() = modify {
        value?.let(::applyProjectTemplate)
    }

    override fun onValueUpdated(reference: SettingReference<*, *>?) {
        super.onValueUpdated(reference)
        if (reference == ProjectTemplatesPlugin::template.reference) {
            applySelectedTemplate()
            value?.let(templateDescriptionComponent::setTemplate)
        }
    }

    override fun onInit() {
        super.onInit()
        if (setting.type.values.isNotEmpty()) {
            list.selectedIndex = 0
            value = setting.type.values.firstOrNull()
        }
    }
}

class TemplateDescriptionComponent : Component() {
    private val descriptionLabel = label("").apply {
        fontColor = UIUtil.FontColor.BRIGHTER
    }

    fun setTemplate(template: ProjectTemplate) {
        descriptionLabel.text = template.description
    }

    override val component: JComponent = descriptionLabel
}