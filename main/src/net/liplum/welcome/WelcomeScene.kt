package net.liplum.welcome

import arc.scene.ui.Dialog
import mindustry.ui.dialogs.BaseDialog

class WelcomeScene(
    @JvmField val id: String,
    @JvmField var iconPath: String = "icon",
    @JvmField var chance: Int = 100,
    @JvmField val condition: WelcomeCondition,
    @JvmField val template: WelcomeTemplate,
) {
    override fun toString() = id

    companion object {
        val Default = WelcomeScene(
            id = "Default",
            condition = WelcomeCondition.Default,
            template = WelcomeTemplate.Default,
            chance = 0,
        )
    }
}

class WelcomeScenePack(
    var default: WelcomeScene,
    var scenes: List<WelcomeScene> = emptyList(),
) {
    operator fun get(index: Int): WelcomeScene =
        if (index !in scenes.indices)
            default
        else
            scenes[index]

    fun indexOf(tipID: WelcomeScene): Int =
        scenes.indexOf(tipID)

    fun inherit(
        default: WelcomeScene? = null,
        scenes: List<WelcomeScene> = emptyList(),
    ): WelcomeScenePack {
        return WelcomeScenePack(
            default = default ?: this.default,
            scenes = this.scenes + scenes,
        )
    }
}

abstract class WelcomeTemplate {
    abstract fun gen(entity: WelcomeEntity): Dialog

    companion object {
        val Default = object : WelcomeTemplate() {
            override fun gen(entity: WelcomeEntity) =
                BaseDialog(entity.bundle["Default.title"]).apply {
                    cont.add(entity.bundle["Default"])
                    addCloseButton()
                }
        }
    }
}

abstract class WelcomeCondition {
    abstract fun canShow(tip: WelcomeScene): Boolean
    abstract fun priority(tip: WelcomeScene): Int

    companion object {
        val Default = object : WelcomeCondition() {
            override fun canShow(tip: WelcomeScene): Boolean {
                return false
            }

            override fun priority(tip: WelcomeScene) = Int.MIN_VALUE
        }
    }
}

abstract class WelcomeAction {
    abstract fun doAction(entity: WelcomeEntity)
    operator fun invoke(entity: WelcomeEntity) {
        doAction(entity)
    }

    companion object {
        val Default = object : WelcomeAction() {
            override fun doAction(entity: WelcomeEntity) {
            }
        }
    }
}
