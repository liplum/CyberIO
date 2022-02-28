package net.liplum.blocks.prism

import arc.graphics.g2d.Draw
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.PowerTurret
import net.liplum.math.PolarPos
import net.liplum.utils.Util2D
import net.liplum.utils.copy

enum class PrismData {
    Duplicate
}

open class Prism(name: String) : PowerTurret(name) {
    //open class Prism(name: String) : Block(name) {
    //copy your lasers
    //
    var realrange = 30f
    var deflection = 15f
    var prismRange = 10f

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
        var selfPos: PolarPos = PolarPos(20f, 0f)
        open val prismX: Float
            get() = x + selfPos.toX()
        open val prismY: Float
            get() = y + selfPos.toY()

        override fun updateTile() {
            selfPos.a += 0.05f * delta()
            //selfPos.r += 0.05f
            Groups.bullet.intersect(
                x - 10f,
                y - 10f,
                realrange,
                realrange
            ) {
                val btype = it.type
                if (btype is LaserBulletType) {
                    it.absorb()
                } else if (btype is BasicBulletType) {
                    if (Util2D.distance(it.x, it.y, prismX, y + prismY) < prismRange) {
                        if (it.data != PrismData.Duplicate) {
                            val angle = it.rotation()
                            it.data = PrismData.Duplicate
                            val copy = it.copy()
                            it.rotation(angle - deflection)
                            copy.rotation(angle + deflection)
                        }
                    }
                }
            }
        }

        override fun draw() {
            Draw.rect(baseRegion, x, y)
            Draw.color()

            Draw.z(Layer.turret)
            tr2.trns(rotation, -recoil)
            val prismX = prismX
            val prismY = prismY
            Drawf.shadow(
                region,
                prismX + tr2.x - elevation,
                prismY + tr2.y - elevation
            )
            Draw.rect(
                region,
                prismX + tr2.x,
                prismY + tr2.y
            )
            Drawf.circles(prismX, prismY, prismRange)
        }
    }
}