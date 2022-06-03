package net.liplum

object Var {
    @JvmField
    var ContentSpecific = ContentSpec.Vanilla
    @JvmField
    var EnableMapCleaner = false
    @JvmField
    var CurDebugLevel = DebugLevel.Any
    @JvmField
    var EnableUnlockContent = false
    /**
     * How much time to reach max selected circle
     */
    @JvmField
    var SelectedCircleTime = 60f
    @JvmField
    var AnimUpdateFrequency = 5f
    @JvmField
    var ShowPowerGraphID = false
    @JvmField
    var DrawBuildCollisionRect = false
    @JvmField
    var DrawUnitCollisionRect = false
    @JvmField
    var EnableEntityInspector = false
    @JvmField
    var ExperimentalMode = false
    @JvmField
    var TestGlCompatibility = false
}