@file:JvmName("DataTransferCatH")

package net.liplum.api.cyber

import arc.scene.ui.Label
import mindustry.world.Block
import mindustry.world.meta.Stat
import net.liplum.R
import net.liplum.common.utils.bundle
import net.liplum.registries.CioStats

fun <T> T.addLinkRangeStats(range: Float) where  T : Block {
    if (range <= 0) return
    stats.add(CioStats.dataRange) { stat ->
        stat.add(Label("$range"))
    }
}

fun <T> T.addLimitedStats(stat: Stat, max: Int) where  T : Block {
    stats.add(stat) {
        it.add(
            if (max < 0) Label(R.Bundle.Unlimited.bundle)
            else Label("$max")
        )
    }
}

fun <T> T.addMaxSenderStats(max: Int) where  T : Block {
    addLimitedStats(CioStats.dataMaxSender, max)
}

fun <T> T.addMaxReceiverStats(max: Int) where  T : Block {
    addLimitedStats(CioStats.dataMaxReceiver, max)
}

fun <T> T.addMaxHostStats(max: Int) where  T : Block {
    addLimitedStats(CioStats.dataMaxHost, max)
}

fun <T> T.addMaxClientStats(max: Int) where  T : Block {
    addLimitedStats(CioStats.dataMaxClient, max)
}