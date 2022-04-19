package net.liplum.blocks.jammer

import arc.Core
import arc.graphics.g2d.Draw
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.LaserTurret
import mindustry.world.consumers.ConsumeType
import mindustry.world.meta.BlockGroup
import net.liplum.CalledBySync
import net.liplum.ClientOnly
import net.liplum.SendDataPack
import net.liplum.lib.Draw
import net.liplum.lib.Observer
import net.liplum.utils.TR
import net.liplum.utils.invoke
import net.liplum.utils.sub

open class Jammer(name: String) : LaserTurret(name) {
    @ClientOnly lateinit var TurretTR: TR
    @ClientOnly lateinit var StereoTR: TR

    init {
        outlineIcon = false
        consumes.remove(ConsumeType.liquid)
        config(Integer::class.java) { obj: JammerBuild, i ->
            if (i.toInt() == 1)
                obj.onJamming()
        }
    }

    override fun load() {
        super.load()
        TurretTR = this.sub("turret")
        StereoTR = this.sub("stereo")
    }

    open inner class JammerBuild : LaserTurretBuild() {
        @SendDataPack
        val wasShootingOb = Observer { wasShooting }.notify { b ->
            if (b) configure(1)
        }
        @CalledBySync
        open fun onJamming() {
            Groups.build.each {
                if (
                    it.team != team &&
                    it.block.group == BlockGroup.logic &&
                    it.dst(this) <= range() * 2
                ) {
                    it.destroyLogic()
                }
            }
        }

        override fun update() {
            super.update()
            wasShootingOb.update()
        }

        override fun draw() {
            baseRegion.Draw(x, y)
            Draw.color()

            Draw.z(Layer.turret)

            tr2.trns(rotation, -recoil)

            Drawf.shadow(TurretTR, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90)
            TurretTR.Draw(x + tr2.x, y + tr2.y, rotation - 90)

            if (Core.atlas.isFound(heatRegion)) {
                heatDrawer(this)
            }
            //TODO: Draw Stereos
            //StereoTR.Draw(x, y)
        }
    }
}