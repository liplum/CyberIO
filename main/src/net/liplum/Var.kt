package net.liplum

import arc.math.Rand
import mindustry.Vars
import net.liplum.ui.NavigationService
import plumy.dsl.WorldXY

object Var {
    @JvmField var Hologram = R.C.Holo
    @JvmField var HologramDark = R.C.HoloDark
    @JvmField var ContentSpecific = ContentSpec.Vanilla
    @JvmField var CurDebugLevel = DebugLevel.Any
    @JvmField var ExperimentalMode = false
    @JvmField var TestGlCompatibility = false
    // Advanced Functions
    @JvmField var EnableMapCleaner = false
    // Visual Effects
    /** How much time to reach max selected circle */
    @JvmField var SelectedCircleTime = 60f
    val MaxRangeCircleTimeFactor = if (Vars.mobile) 1 / 15f else 1f / 12f
    const val CircleStroke = 3f
    @JvmField var SurroundingRectTime = 30f
    @JvmField var AnimUpdateFrequency = 5f
    @JvmField var ParticleEffectNumber = if (Vars.mobile) 64 else 128
    @JvmField val Rand = Rand()
    // Debug Functions
    @JvmField var ShowPowerGraphID = false
    @JvmField var DrawBuildCollisionRect = false
    @JvmField var DrawUnitCollisionRect = false
    @JvmField var EnableEntityInspector = false
    // Data Network
    @JvmField var NetworkNodeRailWidth: WorldXY = Vars.tilesize.toFloat()
    @JvmField var NetworkPayloadSizeInRail: WorldXY = NetworkNodeRailWidth * 2f
    @JvmField var NetworkRailThickness = 0.2f
    @JvmField var DataListMaxItemInRow = 4
    @JvmField var DataListItemSize = Vars.iconLarge * 2.5f
    @JvmField var DataListItemMargin = 5f
    /** [WorldXY] pre tick */
    @JvmField var NetworkNodeRailSpeed: WorldXY = 0.5f * Vars.tilesize
    /** [WorldXY] pre tick */
    @JvmField var NetworkNodeSendingSpeed: WorldXY = 0.5f * Vars.tilesize
    @JvmField val Navigation = NavigationService()
    const val WirelessTowerInitialPingingNumber = 5
    /** unit:tick */
    const val WirelessTowerPingFrequency = 60f * 30f
    const val RsSlightHighlightAlpha = 0.5f
    const val RsColorTransitionTime = 60f
    // P2P
    const val P2pNNodeBalanceThreshold = 2f
    const val P2pArrowRotationTime = 60f
    const val YinYangRotationSpeed = 5f
    @JvmField var GlobalLinkDrawerAlpha = 1f
    const val LinkDrawerTime = 20f
    var HoloWallTintAlpha = 0.6423f // Vanilla as default
    var HoloUnitTintAlpha = 0.404f // Vanilla as default
    const val PrismCrystalPassThroughBloomTime = 12f

    object Data {
        const val UpDownFrameNumber = 7
        const val UpDownDuration = 30f
    }
}