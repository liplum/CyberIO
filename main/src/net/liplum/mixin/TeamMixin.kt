package net.liplum.mixin

import mindustry.Vars
import mindustry.game.Team
import mindustry.gen.Teamc
import mindustry.world.blocks.storage.CoreBlock.CoreBuild

open class TeamMixin : PosMixin(), Teamc {
    @JvmField
    var team: Team = Team.derelict
    override fun inFogTo(viewer: Team) = true
    override fun cheating(): Boolean =
        this.team.rules().cheat

    override fun team(): Team = team
    override fun team(t: Team) {
        team = t
    }

    override fun closestCore(): CoreBuild =
        Vars.state.teams.closestCore(x, y, team)

    override fun closestEnemyCore(): CoreBuild =
        Vars.state.teams.closestEnemyCore(x, y, team)

    override fun core(): CoreBuild = this.team.core()
}