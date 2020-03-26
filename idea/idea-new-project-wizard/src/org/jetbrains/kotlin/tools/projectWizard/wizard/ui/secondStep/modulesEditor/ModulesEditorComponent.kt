package org.jetbrains.kotlin.tools.projectWizard.wizard.ui.secondStep.modulesEditor

import com.intellij.ui.JBColor
import org.jetbrains.kotlin.idea.projectWizard.UiEditorUsageStats
import org.jetbrains.kotlin.tools.projectWizard.core.Context
import org.jetbrains.kotlin.tools.projectWizard.core.entity.ValidationResult
import org.jetbrains.kotlin.tools.projectWizard.core.entity.settings.ListSettingType
import org.jetbrains.kotlin.tools.projectWizard.core.entity.settings.reference
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.KotlinPlugin
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.ProjectKind
import org.jetbrains.kotlin.tools.projectWizard.settings.DisplayableSettingItem
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.Module
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.panel
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.setting.AlwaysShownValidationIndicator
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.setting.SettingComponent
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent

class ModulesEditorComponent(
    context: Context,
    uiEditorUsagesStats: UiEditorUsageStats?,
    needBorder: Boolean,
    editable: Boolean,
    oneEntrySelected: (data: DisplayableSettingItem?) -> Unit,
    selectSettingWithError: (ValidationResult.ValidationError) -> Unit
) : SettingComponent<List<Module>, ListSettingType<Module>>(KotlinPlugin::modules.reference, context) {
    private val tree: ModulesEditorTree =
        ModulesEditorTree(
            onSelected = { oneEntrySelected(it) },
            addModule = { component ->
                val isMppProject = KotlinPlugin::projectKind.value == ProjectKind.Singleplatform
                moduleCreator.create(
                    target = null, // The empty tree case
                    allowMultiplatform = isMppProject,
                    allowSinglepaltformJs = isMppProject,
                    allowAndroid = isMppProject,
                    allowIos = isMppProject,
                    allModules = value ?: emptyList(),
                    createModule = model::add
                )?.showInCenterOf(component)
            }
        )

    private val model = TargetsModel(tree, ::value, context, uiEditorUsagesStats)

    override fun onInit() {
        super.onInit()
        updateModel()
    }

    fun updateModel() {
        model.update()
    }

    private val moduleCreator = NewModuleCreator()

    private val toolbarDecorator = if (editable) ModulesEditorToolbarDecorator(
        tree = tree,
        moduleCreator = moduleCreator,
        model = model,
        getModules = { value ?: emptyList() },
        isMultiplatformProject = { KotlinPlugin::projectKind.value != ProjectKind.Singleplatform }
    ) else null

    override val component: JComponent by lazy(LazyThreadSafetyMode.NONE) {
        panel {
            if (needBorder) {
                border = BorderFactory.createLineBorder(JBColor.border())
            }
            add(if (editable) toolbarDecorator!!.createToolPanel() else tree, BorderLayout.CENTER)
            add(validationIndicator, BorderLayout.SOUTH)
        }
    }

    override val validationIndicator = AlwaysShownValidationIndicator(showText = true) { error ->
        val module = error.target as? Module ?: return@AlwaysShownValidationIndicator
        tree.selectModule(module)
        selectSettingWithError(error)
    }
}
