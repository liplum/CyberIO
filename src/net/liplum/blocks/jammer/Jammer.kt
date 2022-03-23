package net.liplum.blocks.jammer

import arc.Core
import arc.graphics.g2d.Draw
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.LaserTurret
import mindustry.world.blocks.logic.LogicBlock
import mindustry.world.consumers.ConsumeType
import net.liplum.CalledBySync
import net.liplum.ClientOnly
import net.liplum.SendDataPack
import net.liplum.lib.Observer
import net.liplum.utils.TR
import net.liplum.utils.invoke
import net.liplum.utils.sub

open class Jammer(name: String) : LaserTurret(name) {
    @ClientOnly lateinit var TurretTR: TR

    init {
        outlineIcon = false
        configurable = true
        consumes.remove(ConsumeType.liquid)
        config(Integer::class.java) { obj: JammerBuild, i ->
            if (i.toInt() == 1)
                obj.onJamming()
        }
    }

    override fun load() {
        super.load()
        TurretTR = this.sub("turret")
    }

    open inner class JammerBuild : LaserTurretBuild() {
        @SendDataPack
        val wasShootingOb = Observer { wasShooting }.notify { b ->
            if (b) configure(1)
        }
        @CalledBySync
        open fun onJamming() {
            Groups.build.each {
                if (it is LogicBlock.LogicBuild &&
                    it.team != team &&
                    it.dst(this) <= range() * 2
                ) {
                    it.updateCode("")
                }
            }
        }

        override fun update() {
            super.update()
            wasShootingOb.update()
        }

        override fun draw() {
            Draw.rect(baseRegion, x, y)
            Draw.color()

            Draw.z(Layer.turret)

            tr2.trns(rotation, -recoil)

            Drawf.shadow(TurretTR, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90)
            Draw.rect(TurretTR, x + tr2.x, y + tr2.y, rotation - 90)

            if (Core.atlas.isFound(heatRegion)) {
                heatDrawer(this)
            }
        }
    }
}