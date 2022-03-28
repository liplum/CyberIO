package net.liplum.holo

import mindustry.game.Team
import mindustry.gen.Groups

val Team2HoloCapacity: HashMap<Team, Int> = HashMap()
val Team.holoCapacity: Int
    get() = Team2HoloCapacity.computeIfAbsent(this, Team::updateHoloCapacity)

fun Team.updateHoloCapacity(): Int {
    var count = 0
    Groups.build.each {
        if (it.team == this && it is HoloProjector.HoloPBuild) {
            val block = it.block
            if (block is HoloProjector) {
                count += block.holoUnitCapacity
            }
        }
    }
    Team2HoloCapacity[this] = count
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