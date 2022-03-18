package net.liplum.blocks.jammer

import arc.Core
import arc.graphics.g2d.Draw
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.LaserTurret
import mindustry.world.consumers.ConsumeType
import net.liplum.ClientOnly
import net.liplum.utils.TR
import net.liplum.utils.sub

open class Jammer(name: String) : LaserTurret(name) {
    @ClientOnly lateinit var TurretTR: TR

    init {
        outlineIcon = false
        consumes.remove(ConsumeType.liquid)
    }

    override fun load() {
        super.load()
        TurretTR = this.sub("turret")
    }

    open inner class JammerBuild : LaserTurretBuild() {
        override fun draw() {
            Draw.rect(baseRegion, x, y)
            Draw.color()

            Draw.z(Layer.turret)

            tr2.trns(rotation, -recoil)

            Drawf.shadow(TurretTR, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90)
            Draw.rect(TurretTR, x + tr2.x, y + tr2.y, rotation - 90);

            if (Core.atlas.isFound(heatRegion)) {
                heatDrawer[this]
            }
        }
    }
}