package net.liplum.utils

import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.ui.Bar
import mindustry.world.meta.BlockBars
import net.liplum.R

annotation class CioDebugOnly

fun BlockBars.addTeamInfo() {
    this.add<Building>(
        R.Bar.TeamName
    ) {
        Bar(
            { R.Bar.Team.bundle(it.team) },
            { Pal.powerBar },
            { 1f }
        )
    }
}

fun <T> BlockBars.addRangeInfo(maxRange: Float) where T : Building, T : Ranged {
    this.add<T>(
        R.Bar.RangeName
    ) {
        Bar(
            { R.Bar.Range.bundle(it.range()) },
            { Pal.range },
            { it.range() / maxRange }
        )
    }
}