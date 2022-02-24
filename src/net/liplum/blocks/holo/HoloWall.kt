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
import net.liplum.utils.TR
import net.liplum.utils.bundle
import net.liplum.utils.subA

open class HoloWall(name: String) : Wall(name) {
    var restoreReload = 10 * 60f
    lateinit var BaseTR: TR
    lateinit var ImageTR: TR

    init {
        solid = false
        solidifes = true
        canOverdrive = true
        update = true
        buildCostMultiplier = 3f
        hasShadow = false
    }

    override fun loadIcon() {
        fullIcon = this.subA("image")
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
                    { R.Bar.Charge.bundle(it.restoreCharge.toInt()) },
                    { Pal.power },
                    { it.restoreCharge / restoreReload }
                )
            }
        }
    }

    open inner class HoloBuild : WallBuild() {
        var restoreCharge = 0f
        open val isProjecting: Boolean
            get() = health > maxHealth * 0.05f
        open val healthPct: Float
            get() = (health / maxHealth).coerceIn(0f, 1f)
        open var restRestore = 0f
        open var lastDamagedTime = 0f
        override fun collide(other: Bullet): Boolean {
            return isProjecting
        }

        open val canRestructure: Boolean
            get() = lastDamagedTime < restoreReload || !isProjecting

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
                } else {
                    lastDamagedTime = 0f
                }
            }
        }

        override fun draw() {
            //Draw.color(R.C.blendShadowColor)
            //Fill.rect(x, y, G.D(16f),  G.D(16f))
            //Draw.color()
            Draw.rect(BaseTR, x, y)
            //Draw.blend(Blending.additive)
            if (isProjecting) {
                Draw.color(R.C.Holo)
                Draw.alpha(healthPct / 4f * 3f)
                Draw.rect(
                    ImageTR,
                    x /*+ Mathf.random(-0.5f, 0.5f)*/,
                    y /*+ Mathf.random(-0.5f, 0.5f)*/
                )
            }
            //Draw.blend()
            Draw.reset()
            //draw flashing white overlay if enabled
            if (flashHit) {
                if (hit < 0.0001f) return
                Draw.color(flashColor)
                Draw.alpha(hit * 0.5f)
                Draw.blend(Blending.additive)
                Fill.rect(x, y, (Vars.tilesize * size).toFloat(), (Vars.tilesize * size).toFloat())
                Draw.blend()
                Draw.reset()
                if (!Vars.state.isPaused) {
                    hit = Mathf.clamp(hit - Time.delta / 10f)
                }
            }
        }

        open val canRestore: Boolean
            get() = !isProjecting || health < maxHealth

        override fun updateTile() {
            if (restoreCharge < restoreReload && canRestructure) {
                restoreCharge += delta()
            }
            if (restRestore > 0.01f) {
                val restored = restRestore * delta() * 0.03f
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
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            restoreCharge = read.f()
            restRestore = read.f()
        }
    }
}