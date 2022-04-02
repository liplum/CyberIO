package net.liplum

import arc.scene.ui.Label
import mindustry.ui.dialogs.BaseDialog
import net.liplum.utils.inCio

object Welcome {
    @JvmStatic
    fun showWelcomeDialog() {
        show()
    }

    @JvmStatic
    fun show() {
        val dialog = BaseDialog(News.getTitle())
        dialog.cont.image("icon".inCio())
            .maxSize(200f).pad(20f).row()
        val welcomeLabel = Label(News.getWelcome()).apply {
            setAlignment(0)
            setWrap(true)
        }
        dialog.cont.add(welcomeLabel)
            .growX()
            .row()
        val newsLabel = Label(News.getNews()).apply {
            setAlignment(0)
            setWrap(true)
        }
        dialog.cont.add(newsLabel)
            .growX()
            .row()
        dialog.cont.button(News.getRead()) {
            dialog.hide()
        }.size(100f, 50f)
        dialog.layout()
        dialog.show()
    }
}