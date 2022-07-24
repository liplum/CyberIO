package net.liplum

import arc.math.Rand
import mindustry.Vars
import net.liplum.mdt.utils.WorldXY
import net.liplum.ui.NavigationService

object Var {
    @JvmField var ContentSpecific = ContentSpec.Vanilla
    @JvmField var CurDebugLevel = DebugLevel.Any
    @JvmField var ExperimentalMode = false
    @JvmField var TestGlCompatibility = false
    // Advanced Functions
    @JvmField var EnableMapCleaner = false
    // Visual Effects
    /** How much time to reach max selected circle */
    @JvmField var SelectedCircleTime = 60f
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
    const val wirelessTowerInitialPingingNumber = 5
    const val rsSlightHighlightAlpha = 0.5f
    const val CyberColorTransitionTime = 60f
}