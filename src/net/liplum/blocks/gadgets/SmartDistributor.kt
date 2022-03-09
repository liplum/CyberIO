package net.liplum.blocks.gadgets

import arc.math.Mathf
import net.liplum.ClientOnly
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.blocks.AniedBlock

private typealias AniStateD = AniState<SmartDistributor, SmartDistributor.SmartDISBuild>

open class SmartDistributor(name: String) : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>(name) {
    @ClientOnly lateinit var RequireAni: AniStateD
    @ClientOnly lateinit var NoPowerAni: AniStateD

    init {
        solid = true
        update = true
        hasItems = true
    }

    open inner class SmartDISBuild : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>.AniedBuild() {

    }

    override fun genAniConfig() {
        config {
            From(RequireAni) To NoPowerAni When { _, build ->
                Mathf.zero(build.power.status)
            }
            From(NoPowerAni) To RequireAni When { _, build ->
                !Mathf.zero(build.power.status)
            }
        }
    }

    override fun genAniState() {
        RequireAni = addAniState("Require")
        NoPowerAni = addAniState("NoPower")
    }

}