package net.liplum.blocks.prism

import arc.Core
import arc.graphics.g2d.Draw
import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.PowerTurret
import net.liplum.math.PolarPos

open class Prism(name: String) : PowerTurret(name) {
    //open class Prism(name: String) : Block(name) {
    //copy your lasers
    //
    var realrange = 30f

    init {
        solid = true
        update = true
        absorbLasers = true
    }

    open inner class PrismLaser : LaserBulletType() {
        init {
        }
    }

    open inner class PrismBuild : PowerTurretBuild() {
        var selfPos: PolarPos = PolarPos(10f,0f)
        override fun updateTile() {
            Groups.bullet.intersect(
                x - 10f,
                y - 10f,
                realrange,
                realrange
            ) {
                if (it.type is LaserBulletType) {
                    it.absorb()
                }
            }
        }

        override fun draw() {
            Draw.rect(baseRegion, x, y)
            Draw.color()

            Draw.z(Layer.turret)
            tr2.trns(rotation, -recoil)
            selfPos.a += 0.1f
            selfPos.r += 0.05f
            Drawf.shadow(
                region,
                selfPos.toX() + x + tr2.x - elevation,
                selfPos.toY() + x + tr2.y - elevation
            )
            Draw.rect(
                region,
                selfPos.toX() + x + tr2.x,
                selfPos.toY() + y + tr2.y
            )
        }
    }
}