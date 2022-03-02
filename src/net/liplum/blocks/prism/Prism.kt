package net.liplum.blocks.prism

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.entities.bullet.ContinuousLaserBulletType
import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.animations.anims.Animation
import net.liplum.draw
import net.liplum.math.PolarPos
import net.liplum.persistance.polarPos
import net.liplum.utils.*

enum class PrismData {
    Duplicate
}

open class Prism(name: String) : Turret(name) {
    lateinit var PrismAnim: Animation
    var realRange = 30f
    var deflectionAngle = 15f
    var prismRange = 10f
    var prismRevolutionSpeed = 0.05f
    @ClientOnly var prismRotationSpeed = 0.05f

    init {
        solid = true
        update = true
        absorbLasers = true
        rotateSpeed = prismRevolutionSpeed * 200
    }

    override fun load() {
        super.load()
        PrismAnim = this.autoAnim(frame = 7, totalDuration = 60f)
    }

    open inner class PrismLaser : LaserBulletType() {
        init {
        }
    }

    open inner class PrismBuild : TurretBuild(), ControlBlock {
        var prismRevolution: PolarPos = PolarPos(realRange - prismRange, 0f)
        var prismRotation: PolarPos = PolarPos(prismRange, 0f)
        open val prismX: Float
            get() = x + prismRevolution.toX()
        open val prismY: Float
            get() = y + prismRevolution.toY()

        override fun updateTile() {
            if (isControlled) {
                val ta = PolarPos.toA(unit.aimX() - x, unit.aimY() - y)
                prismRevolution.a =
                    Angles.moveToward(
                        prismRevolution.a.degree, ta.degree,
                        rotateSpeed * delta()
                    ).radian
            } else {
                prismRevolution.a += prismRevolutionSpeed * delta()
            }
            prismRotation.a -= prismRotationSpeed * delta()
            Groups.bullet.intersect(
                x - realRange / 2f,
                y - realRange / 2f,
                realRange,
                realRange
            ) {
                val btype = it.type
                if (btype !is ContinuousLaserBulletType) {
                    if (Util2D.distance(it.x, it.y, prismX, prismY) < prismRange) {
                        if (it.data != PrismData.Duplicate) {
                            it.data = PrismData.Duplicate
                            val angle = it.rotation()
                            val copy = it.copy()
                            val degree = prismRotation.a.degree
                            it.rotation(angle - deflectionAngle)
                            copy.rotation(angle + deflectionAngle)
                            /*it.rotation(degree - deflectionAngle)
                            copy.rotation(degree + deflectionAngle)*/
                            /*it.rotation(angle - degree)
                            copy.rotation(angle - degree)*/
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
            tr2.trns(rotation, -recoil)
            Draw.z(Layer.blockOver)
            Drawf.shadow(
                PrismAnim[0],
                prismX + tr2.x - elevation * 7f,
                prismY + tr2.y - elevation * 7f,
                prismRotation.a.degree.draw
            )
            Draw.z(Layer.turret)
            Draw.rect(
                PrismAnim[0],
                prismX + tr2.x,
                prismY + tr2.y,
                prismRotation.a.degree.draw
            )
            /*
                PrismAnim.draw {
                Draw.z(Layer.blockOver)
                Drawf.shadow(
                    it,
                    prismX + tr2.x - elevation * 7f,
                    prismY + tr2.y - elevation * 7f
                )
                Draw.z(Layer.turret)
                Draw.rect(
                    it,
                    prismX + tr2.x,
                    prismY + tr2.y
                )
            }*/
            DebugOnly {
                Draw.z(Layer.overlayUI)
                Drawf.circles(prismX, prismY, prismRange)
            }
        }

        override fun drawSelect() {
            Drawf.dashCircle(x, y, realRange, team.color)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            prismRevolution = read.polarPos()
            prismRotation = read.polarPos()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.polarPos(prismRevolution)
            write.polarPos(prismRotation)
        }
    }
}