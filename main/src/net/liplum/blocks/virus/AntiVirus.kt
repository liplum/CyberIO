package net.liplum.blocks.virus

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Effect
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import net.liplum.render.G
import net.liplum.render.G.realHeight
import net.liplum.render.G.realWidth
import net.liplum.render.drawEffectCirclePlace
import net.liplum.ui.bars.ReverseBar
import net.liplum.utils.addRangeInfo
import net.liplum.utils.sub
import plumy.core.ClientOnly
import plumy.core.arc.Tick
import plumy.core.arc.toSecond
import plumy.core.assets.EmptyTR
import plumy.core.math.isZero
import plumy.dsl.bundle

internal const val T2SD = 5f / 6f * Mathf.pi
internal const val HalfPi = 1f / 2f * Mathf.pi
internal const val Left = HalfPi - 0.5f
internal const val Right = HalfPi + 0.5f
internal const val VirusEliminatePct = Left / T2SD
internal var SinLeft = Mathf.sin(HalfPi + 0.5f)
internal fun T2S(pct: Float): Float {
    val x = pct * T2SD
    if (x in Left..Right) {
        return SinLeft
    }
    if (pct in 0f..1f) {
        return Mathf.sin(x)
    }
    return Mathf.sin(T2SD)
}

internal const val ShieldExpandEffectDuration = 70f
internal const val ShieldExpandingDuration = 60f
internal const val VirusEliminatePoint = VirusEliminatePct * ShieldExpandingDuration
val ShieldExpand: Effect = Effect(ShieldExpandEffectDuration) {
    val avb = it.data as AntiVirus.AntiVirusBuild
    val av = avb.block as AntiVirus
    val s = av.shieldTR
    val d2s = T2S(it.time / ShieldExpandingDuration)
    Draw.alpha(it.fout())
    val scale = avb.realRange / 15f
    Draw.rect(
        s, it.x, it.y,
        s.realWidth * d2s * scale,
        s.realHeight * d2s * scale
    )
}.layer(Layer.power)

fun AntiVirus.AntiVirusBuild.shieldExpanding() {
    ShieldExpand.at(x, y, realRange, this)
}

open class AntiVirus(name: String) : Block(name) {
    @JvmField var range: Float = 60f
    @JvmField var reload: Tick = 120f
    @JvmField var maxCoolDown: Tick = 240f
    @JvmField var minPrepareTime: Tick = 30f
    @JvmField var heatRate = 0.1f
    @JvmField var shieldExpendMinInterval = ShieldExpandEffectDuration * 0.6f
    @JvmField var uninfectedColor: Color = R.C.GreenSafe
    @JvmField var infectedColor: Color = R.C.RedAlert
    @ClientOnly @JvmField var unenergizedTR = EmptyTR
    @ClientOnly @JvmField var shieldTR = EmptyTR
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime

    init {
        buildType = Prov { AntiVirusBuild() }
        solid = true
        update = true
        updateInUnits = true
        alwaysUpdateInUnits = true
        group = BlockGroup.projectors
        hasPower = true
    }

    override fun load() {
        super.load()
        unenergizedTR = this.sub("unenergized")
        shieldTR = this.sub("shield")
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addRangeInfo<AntiVirusBuild>(100f)
        }
        addBar<AntiVirusBuild>(R.Bar.CoolDownN) {
            ReverseBar(
                { R.Bar.CoolDown.bundle(it.coolDown.toSecond) },
                { R.C.CoolDown },
                {
                    if (it.coolDown > reload)
                        it.coolDown / maxCoolDown
                    else it.coolDown / reload
                }
            )
        }
    }

    override fun setStats() {
        super.setStats()
        stats.add(Stat.range, range, StatUnit.blocks)
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond)
    }

    override fun canReplace(other: Block) = super.canReplace(other) || other is Virus
    override fun minimapColor(tile: Tile) = R.C.GreenSafe.rgba()
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        val range = range * smoothPlacing(maxSelectedCircleTime)
        drawEffectCirclePlace(x, y, uninfectedColor, range) {
            G.selectedBreath(this, getOtherColor(this))
        }
    }

    open fun getOtherColor(other: Building): Color {
        return if (other is IVirusBuilding) infectedColor
        else uninfectedColor
    }

    open inner class AntiVirusBuild : Building(), Ranged {
        open val realRange: Float
            get() = range * efficiency() * Mathf.log(3f, timeScale + 2f)
        open var coolDown: Float = reload
            set(value) {
                field = value.coerceIn(0f, maxCoolDown)
            }

        open fun heat() {
            coolDown += reload * heatRate
        }

        open var shieldExpendCharge = 0f
        override fun updateTile() {
            coolDown -= edelta()
            ClientOnly {
                shieldExpendCharge += Time.delta
            }
            if (coolDown <= 0f) {
                var eliminated = false
                Vars.indexer.eachBlock(this, realRange,
                    { b ->
                        b is IVirusBuilding && !b.isDead
                    }) {
                    Time.runTask(
                        (VirusEliminatePoint + Mathf.random(-10, 10))
                            .coerceAtLeast(0f)
                    ) {
                        (it as? IVirusBuilding)?.killVirus()
                        ClientOnly {
                            Fx.breakBlock.at(it)
                        }
                        heat()
                    }
                    eliminated = true
                }
                val realRange2 = realRange * 2f
                Groups.bullet.intersect(
                    x - realRange,
                    y - realRange,
                    realRange2,
                    realRange2
                ) {
                    if (absorbBullet(it)) {
                        eliminated = true
                        heat()
                    }
                }
                if (eliminated) {
                    coolDown = reload
                }
                ClientOnly {
                    if (eliminated && shieldExpendCharge >= shieldExpendMinInterval) {
                        shieldExpendCharge = 0f
                        this.shieldExpanding()
                    }
                }
            }
        }

        override fun draw() {
            super.draw()
            if (power.status.isZero) {
                Draw.rect(unenergizedTR, x, y)
            }
        }

        override fun drawSelect() {
            if (!power.status.isZero) {
                val range = realRange * smoothSelect(maxSelectedCircleTime)
                Vars.indexer.eachBlock(this, range,
                    {
                        true
                    }) {
                    G.selectedBreath(it, getOtherColor(it))
                }
                Drawf.dashCircle(x, y, range, uninfectedColor)
            }
        }

        override fun range(): Float = realRange
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            coolDown = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.f(coolDown)
        }

        fun absorbBullet(bullet: Bullet): Boolean {
            if (bullet.team != team &&
                bullet.type.absorbable &&
                bullet.dst(this) <= realRange
            ) {
                bullet.absorb()
                Fx.absorb.at(bullet)
                return true
            }
            return false
        }
    }
}