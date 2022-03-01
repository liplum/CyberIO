package net.liplum.blocks.prism

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.math.PolarPos
import net.liplum.persistance.polarPos
import net.liplum.utils.*

enum class PrismData {
    Duplicate
}

open class Prism(name: String) : Turret(name) {
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
        rotateSpeed = prismASpeed * 300
    }

    open inner class PrismLaser : LaserBulletType() {
        init {
        }
    }

    open inner class PrismBuild : TurretBuild(), ControlBlock {
        var selfPolarPos: PolarPos = PolarPos(realRange - prismRange, 0f)
        open val prismX: Float
            get() = x + selfPolarPos.toX()
        open val prismY: Float
            get() = y + selfPolarPos.toY()

        override fun updateTile() {
            if (isControlled) {
                val ta = PolarPos.toA(unit.aimX() - x, unit.aimY() - y)
                selfPolarPos.a =
                    Angles.moveToward(
                        selfPolarPos.a.degree, ta.degree,
                        rotateSpeed * delta()
                    ).radian
            } else {
                selfPolarPos.a += prismASpeed * delta()
            }
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
            val prismX = prismX
            val prismY = prismY
            Draw.z(Layer.blockOver)
            Drawf.shadow(
                region,
                prismX + tr2.x - elevation * 7f,
                prismY + tr2.y - elevation * 7f
            )
            Draw.z(Layer.turret)
            tr2.trns(rotation, -recoil)
            Draw.rect(
                region,
                prismX + tr2.x,
                prismY + tr2.y
            )
            Draw.z(Layer.overlayUI)
            Drawf.circles(prismX, prismY, prismRange)
        }

        override fun drawSelect() {
            Drawf.dashCircle(x, y, realRange, team.color)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            selfPolarPos = read.polarPos()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.polarPos(selfPolarPos)
        }
    }
}