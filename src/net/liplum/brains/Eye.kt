package net.liplum.brains

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.geom.Vec2
import mindustry.Vars
import mindustry.entities.bullet.BulletType
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.PowerTurret
import net.liplum.ClientOnly
import net.liplum.api.brain.Direction4
import net.liplum.api.brain.IBrain
import net.liplum.api.brain.IUpgradeComponent
import net.liplum.draw
import net.liplum.lib.animations.anis.Draw
import net.liplum.utils.TR
import net.liplum.utils.addBrainInfo
import net.liplum.utils.sheet
import net.liplum.utils.sub

open class Eye(name: String) : PowerTurret(name) {
    lateinit var normalBullet: BulletType
    lateinit var improvedBullet: BulletType
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var EyeBallTR: TR
    @ClientOnly lateinit var EyelidTR: TR
    @ClientOnly lateinit var PupilTR: TR
    @ClientOnly lateinit var PupilOutsideTR: TR
    @ClientOnly lateinit var HemorrhageTR: Array<TR>
    override fun init() {
        // To prevent accessing a null
        shootType = normalBullet
        super.init()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        EyeBallTR = this.sub("eyeball")
        EyelidTR = this.sub("eyelid")
        PupilTR = this.sub("pupil")
        PupilOutsideTR = this.sub("pupil-outside")
        HemorrhageTR = this.sheet("hemorrhage", 3)
    }

    override fun icons() = arrayOf(
        BaseTR, EyeBallTR
    )

    override fun setBars() {
        super.setBars()
        bars.addBrainInfo<EyeBuild>()
    }

    open inner class EyeBuild : PowerTurretBuild(), IUpgradeComponent {
        override var directionInfo: Direction4 = Direction4.Empty
        override var brain: IBrain? = null
        override fun onProximityRemoved() {
            super.onProximityRemoved()
            clear()
        }

        override fun remove() {
            super.remove()
            clear()
        }
        @ClientOnly
        val sight = Vec2(1.2f, 0f)
        override fun draw() {
            BaseTR.Draw(x, y)
            Draw.color()

            Draw.z(Layer.turret)

            tr2.trns(rotation, -recoil)
            val rotation = rotation.draw
            Drawf.shadow(EyeBallTR, x - elevation, y - elevation, rotation)
            EyeBallTR.Draw(x, y, rotation)
            if (isShooting) {
                PupilTR.Draw(x - tr2.x, y - tr2.y, rotation)
            } else {
                val player = Vars.player.unit()
                sight.setAngle(
                    Angles.angle(
                        x, y,
                        player.x, player.y
                    )
                )
                PupilTR.Draw(
                    x - tr2.x + sight.x,
                    y - tr2.y + sight.y,
                    rotation
                )
            }
            drawHemorrhage()
        }

        open fun drawHemorrhage() {
            val index = when (timeScale) {
                in 0f..1f -> 0
                in 1.01f..2.499f -> 1
                else -> 2
            }
            val hemorrhage = HemorrhageTR[index]
            Draw.alpha(heat)
            hemorrhage.Draw(x, y, rotation.draw)
            Draw.color()
        }

        override fun hasAmmo() = true
        override fun useAmmo() =
            if (isLinkedBrain) improvedBullet else normalBullet

        override fun peekAmmo() =
            if (isLinkedBrain) improvedBullet else normalBullet
    }
}