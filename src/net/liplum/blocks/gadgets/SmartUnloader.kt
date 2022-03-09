package net.liplum.blocks.gadgets

import arc.math.Mathf
import net.liplum.ClientOnly
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.blocks.AniedBlock

private typealias AniStateU = AniState<SmartUnloader, SmartUnloader.SmartULDBuild>

open class SmartUnloader(name: String) : AniedBlock<SmartUnloader, SmartUnloader.SmartULDBuild>(name) {
    @ClientOnly lateinit var RequireAni: AniStateU
    @ClientOnly lateinit var NoPowerAni: AniStateU

    init {
        solid = true
        update = true
        hasItems = true
    }

    open inner class SmartULDBuild : AniedBlock<SmartUnloader, SmartUnloader.SmartULDBuild>.AniedBuild() {

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