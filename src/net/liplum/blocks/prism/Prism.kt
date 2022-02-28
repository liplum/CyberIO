package net.liplum.blocks.prism

import arc.graphics.g2d.Draw
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.PowerTurret
import net.liplum.math.PolarPos
import net.liplum.utils.Util2D
import net.liplum.utils.copy
import net.liplum.utils.polarPos

enum class PrismData {
    Duplicate
}

open class Prism(name: String) : PowerTurret(name) {
    //open class Prism(name: String) : Block(name) {
    //copy your lasers
    //
    var realRange = 30f
    var deflectionAngle = 15f
    var prismRange = 10f
    var prismASpeed = 0.05f

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
            selfPos.a += prismASpeed * delta()
            //selfPos.r += 0.05f
            Groups.bullet.intersect(
                x - realRange / 2f,
                y - realRange / 2f,
                realRange,
                realRange
            ) {
                val btype = it.type
                if (btype is LaserBulletType) {
                    it.absorb()
                } else if (btype is BasicBulletType) {
                    if (Util2D.distance(it.x, it.y, prismX, prismY) < prismRange) {
                        if (it.data != PrismData.Duplicate) {
                            val angle = it.rotation()
                            it.data = PrismData.Duplicate
                            val copy = it.copy()
                            it.rotation(angle - deflectionAngle)
                            copy.rotation(angle + deflectionAngle)
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

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            selfPos = read.polarPos()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.polarPos(selfPos)
        }
    }
}