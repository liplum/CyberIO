package net.liplum

object Var {
    @JvmField
    var EnableMapCleaner = false
    /**
     * How much time to reach max selected circle
     */
    @JvmField
    var selectedCircleTime = 60f
    @JvmField
    var UpdateFrequency = 5f
    @JvmField
    var ShowPowerGraphID = false
    @JvmField
    var DrawBuildCollisionRect = false
    @JvmField
    var DrawUnitCollisionRect = false
    @JvmStatic
    var DebugMode = Meta.EnableDebug
        set(value) {
            field = Meta.EnableDebug && value
        }
    @JvmField
    var ExperimentalMode = false
    @JvmField
    var TestGlCompatibility = false
}