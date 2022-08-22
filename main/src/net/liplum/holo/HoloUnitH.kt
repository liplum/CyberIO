package net.liplum.holo

import mindustry.game.Team
import net.liplum.annotations.SubscribeEvent
import net.liplum.event.BattleExitEvent

val Team2HoloCapacity: HashMap<Team, Int> = HashMap()
val Team.holoCapacity: Int
    get() = Team2HoloCapacity.getOrPut(this) { this.updateHoloCapacity() }
@SubscribeEvent(BattleExitEvent::class)
fun clearAllCacheWhenExit() {
    Team2HoloCapacity.clear()
}
/**
 * @param thisProjector if non-null, this projector will be counted.
 */
fun Team.updateHoloCapacity(
    thisProjector: HoloProjector.HoloProjectorBuild? = null,
): Int {
    var count = thisProjector?.block()?.holoUnitCapacity ?: 0
    val buildings = data().buildings
    if (buildings.isEmpty) return 0
    buildings.forEach {
        if (it is HoloProjector.HoloProjectorBuild) {
            if (it.isAdded && it.isValid) {
                val block = it.block
                if (block is HoloProjector && it != thisProjector) {
                    count += block.holoUnitCapacity
                }
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