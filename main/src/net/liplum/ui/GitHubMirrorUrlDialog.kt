package net.liplum.ui

import arc.Core
import arc.scene.ui.TextButton
import arc.scene.ui.TextField
import arc.util.Http
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog
import net.liplum.Meta
import net.liplum.R
import net.liplum.Settings
import net.liplum.common.delegate.Delegate
import net.liplum.common.ing
import net.liplum.common.util.IBundlable
import net.liplum.common.util.bundle
import net.liplum.common.util.useFakeHeader
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.ui.ShowTextDialog
import net.liplum.mdt.ui.addTrackTooltip

@ClientOnly
object GitHubMirrorUrlDialog : IBundlable {
    override val bundlePrefix: String = R.Advanced.GithubMirror
    @JvmStatic
    fun show(onReset: Delegate) {
        BaseDialog(bundle("dialog")).apply {
            val field = TextField(
                Settings.GitHubMirrorUrl, Styles.defaultField
            ).addTrackTooltip(bundle("field-tooltip"))
            onReset.add {
                Settings.GitHubMirrorUrl = Meta.GitHubUrl
                field.text = Meta.GitHubUrl
            }
            cont.add(field).width((Core.graphics.width / 1.2f).coerceAtMost(460f)).row()
            cont.table { t ->
                fun onFailed(error: String) {
                    ShowTextDialog(bundle("failed", field.text, error))
                }

                fun onSucceeded() {
                    val url = field.text.trim('\\').trim('/')
                    Settings.GitHubMirrorUrl = url
                    field.text = url
                    ShowTextDialog(bundle("success", url))
                }

                fun onResetDefault() {
                    Settings.GitHubMirrorUrl = Meta.GitHubUrl
                    field.text = Meta.GitHubUrl
                    ShowTextDialog(bundle("reset", field.text))
                }

                val saveButton = TextButton("@save").apply {
                    update {
                        label.setText(if (isDisabled) R.Ctrl.Validate.bundle.ing else "@save")
                    }
                    changed {
                        if (field.text.isEmpty()) {
                            onResetDefault()
                        } else {
                            isDisabled = true
                            Http.get(field.text).useFakeHeader().error { e ->
                                onFailed("${e.javaClass.name} ${e.message}")
                                isDisabled = false
                            }.submit { rep ->
                                Core.app.post {
                                    if (rep.status == Http.HttpStatus.OK)
                                        onSucceeded()
                                    else
                                        onFailed(rep.status.name)
                                    isDisabled = false
                                }
                            }
                        }
                    }
                    addTrackTooltip(bundle("save-tooltip"))
                }
                t.add(saveButton).size(200f, 50f)
                t.button("@cancel") {
                    field.text = Settings.GitHubMirrorUrl
                }.size(200f, 50f).get().apply {
                    addTrackTooltip(bundle("cancel-tooltip"))
                }
            }
            cont.row()
            addCloseButton()
        }.show()
    }
}