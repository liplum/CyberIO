package net.liplum.holo

import mindustry.game.Team
import mindustry.gen.Groups

val Team.holoCapacity: Int
    get() {
        var count = 0
        Groups.build.each {
            if (it.team == this && it is HoloProjector.HoloPBuild) {
                val block = it.block
                if (block is HoloProjector) {
                    count += block.holoUnitCapacity
                }
            }
        }
        return count
    }

fun HoloUnitType.canCreateHoloUnitIn(team: Team): Boolean {
    val total = team.data().countType(this)
    return total < team.holoCapacity
}

fun Team.getStringHoloCap(): String {
    val cap = this.holoCapacity
    return if (cap >= Int.MAX_VALUE - 1) "∞" else cap.toString() + ""
}

fun HoloUnitType.pctOfTeamOwns(team: Team) =
    team.data().countType(this).toFloat() / team.holoCapacity