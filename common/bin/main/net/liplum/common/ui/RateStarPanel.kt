package net.liplum.common.ui

import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.ImageButton
import arc.scene.ui.layout.Table
import plumy.core.assets.TR

class RateStarPanelBuilder {
    var starNumber = 5
    var starSize = 50f
    var inactiveStar: TR = TR()
    var activeStar: TR = TR()
    inline fun build(
        crossinline onRate: (Int) -> Unit = {},
    ): Table = Table().apply {
        var activeNumber = 0
        for (i in 0 until starNumber) {
            val curNumber = i
            val starImg = TextureRegionDrawable(inactiveStar)
            add(ImageButton(starImg).apply {
                changed {
                    activeNumber = curNumber
                    onRate(activeNumber)
                }
                update {
                    starImg.region =
                        if (curNumber <= activeNumber) activeStar else inactiveStar
                }
            }).size(starSize)
        }
    }
}