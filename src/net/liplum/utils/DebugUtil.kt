package net.liplum.utils

import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.ui.Bar
import mindustry.world.meta.BlockBars
import net.liplum.R

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
