package net.liplum.blocks.holo

import arc.graphics.Blending
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Mathf
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Call
import mindustry.graphics.Pal
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.blocks.defense.Wall
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.toSecond
import net.liplum.utils.*

private const val FloatingRange = 0.6f

open class HoloWall(name: String) : Wall(name) {
    var restoreReload = 10 * 60f
    lateinit var BaseTR: TR
    lateinit var ImageTR: TR
    var minHealthProportion = 0.05f
    var maxSleepyTime = 30 * 60f

    init {
        solid = false
        solidifes = true
        canOverdrive = true
        update = true
        hasShadow = false
        absorbLasers = true
        flashHit = true
    }

    override fun loadIcon() {
        fullIcon = this.subA("ui")
        uiIcon = fullIcon
    }

    override fun load() {
        BaseTR = this.subA("base")
        ImageTR = this.subA("image")
        region = BaseTR
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.add<HoloBuild>(R.Bar.IsProjectingN) {
                Bar(
                    { R.Bar.IsProjecting.bundle(it.isProjecting) },
                    { Pal.bar },
                    { if (it.isProjecting) 1f else 0f }
                )
            }
            bars.add<HoloBuild>(R.Bar.RestRestoreN) {
                Bar(
                    { R.Bar.RestRestore.bundle(it.restRestore.toInt()) },
                    { Pal.bar },
                    { it.restRestore / it.maxHealth }
                )
            }
            bars.add<HoloBuild>(R.Bar.ChargeN) {
                Bar(
                    { R.Bar.Charge.bundle(it.restoreCharge.toSecond()) },
                    { Pal.power },
                    { it.restoreCharge / restoreReload }
                )
            }
            bars.add<HoloBuild>(R.Bar.LastDamagedN) {
                Bar(
                    { R.Bar.LastDamaged.bundle(it.lastDamagedTime.toSecond()) },
                    { Pal.power },
                    { it.lastDamagedTime / restoreReload }
                )
            }
            bars.addSleepInfo()
        }
    }

    open inner class HoloBuild : WallBuild() {
        var restoreCharge = 0f
        open val isProjecting: Boolean
            get() = health > maxHealth * minHealthProportion
        open val healthPct: Float
            get() = (health / maxHealth).coerceIn(0f, 1f)
        open var restRestore = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        open var lastDamagedTime = 0f
        var xOffset = Mathf.random(-FloatingRange, FloatingRange)
            set(value) {
                field = value.coerceIn(-FloatingRange, FloatingRange)
            }
        var xAdding = false
        var yOffset = Mathf.random(-FloatingRange, FloatingRange)
            set(value) {
                field = value.coerceIn(-FloatingRange, FloatingRange)
            }
        var yAdding = false
        override fun collide(other: Bullet): Boolean {
            return isProjecting
        }

        open val canRestructure: Boolean
            get() = lastDamagedTime > restoreReload || !isProjecting

        override fun create(block: Block, team: Team): Building {
            super.create(block, team)
            this.team = Team.derelict
            return this
        }

        override fun damage(damage: Float) {
            if (!this.dead()) {
                val dm = Vars.state.rules.blockHealth(team)
                var d = damage
                if (Mathf.zero(dm)) {
                    d = this.health + 1.0f
                } else {
                    d /= dm
                }

                Call.tileDamage(this, this.health - handleDamage(d))
                if (this.health <= 0.0f) {
                    //Call.tileDestroyed(this)
                }
                lastDamagedTime = 0f
                noSleep()
            }
        }

        override fun draw() {
            //Draw.color(R.C.blendShadowColor)
            //Fill.rect(x, y, G.D(16f),  G.D(16f))
            //Draw.color()
            Draw.rect(BaseTR, x, y)
            //Draw.blend(Blending.additive)
            updateFloating()
            if (isProjecting) {
                Draw.color(R.C.Holo)
                Draw.alpha(healthPct / 4f * 3f)
                Draw.rect(
                    ImageTR,
                    x + xOffset,
                    y + yOffset
                )
            }
            //Draw.blend()
            Draw.reset()
            //draw flashing white overlay if enabled
            if (flashHit) {
                if (hit < 0.0001f) return
                Draw.color(flashColor)
                Draw.alpha(hit * 0.5f * healthPct)
                Draw.blend(Blending.additive)
                Fill.rect(x, y, (Vars.tilesize * size).toFloat(), (Vars.tilesize * size).toFloat())
                Draw.blend()
                Draw.reset()
                if (!Vars.state.isPaused) {
                    hit = Mathf.clamp(hit - Time.delta / 10f)
                }
            }
        }

        open fun updateFloating() {
            val d = G.D(0.1f * FloatingRange * delta())
            if (xAdding) {
                xOffset += d
            } else {
                xOffset -= d
            }
            if (xOffset == -FloatingRange || xOffset == FloatingRange) {
                xAdding = !xAdding
            }

            if (yAdding) {
                yOffset += d
            } else {
                yOffset -= d
            }
            if (yOffset == -FloatingRange || yOffset == FloatingRange) {
                yAdding = !yAdding
            }
        }

        open val canRestore: Boolean
            get() = !isProjecting || health < maxHealth
        open val isRecovering: Boolean
            get() = restRestore > 0.5f

        override fun updateTile() {
            lastDamagedTime += delta()
            if (restoreCharge < restoreReload && !isRecovering && canRestructure) {
                restoreCharge += delta()
            }
            if (isRecovering) {
                val restored = if (restRestore <= maxHealth * minHealthProportion)
                    restRestore
                else
                    restRestore * delta() * 0.01f
                health = health.coerceAtLeast(0f)
                heal(restored)
                restRestore -= restored
            }

            if (canRestore && restoreCharge >= restoreReload) {
                restoreCharge = 0f
                if (health != maxHealth) {
                    dead = false
                    restRestore = maxHealth
                }
            }
            if (!isRecovering &&
                !canRestore &&
                restoreCharge >= restoreReload &&
                lastDamagedTime >= maxSleepyTime
            ) {
                sleep()
            }
        }

        override fun drawCracks() {
        }

        override fun checkSolid(): Boolean {
            return isProjecting
        }

        override fun write(write: Writes) {
            super.write(write)
            write.f(restoreCharge)
            write.f(restRestore)
            write.f(lastDamagedTime)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            restoreCharge = read.f()
            restRestore = read.f()
            lastDamagedTime = read.f()
        }
    }
}