package net.liplum.blocks.underdrive

import mindustry.gen.Building

typealias UOR = UnderdriveUtil.OverwriteRule

fun Building.applyBoostOrSlow(laxityOrIntensity: Float, duration: Float, overwriteRule: UOR = UOR.Coerce) {
    UnderdriveUtil.applyBoostOrSlow(this, laxityOrIntensity, duration, overwriteRule)
}

fun Building.resetBoost() {
    UnderdriveUtil.resetBoost(this)
}