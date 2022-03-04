package net.liplum.blocks.virus

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
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.seconds
import net.liplum.ui.bars.ReverseBar
import net.liplum.utils.*

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
        G.Dw(s) * d2s * scale,
        G.Dh(s) * d2s * scale
    )
}.layer(Layer.power)

fun AntiVirus.AntiVirusBuild.shieldExpanding() {
    ShieldExpand.at(x, y, realRange, this)
}

open class AntiVirus(name: String) : Block(name) {
    @JvmField var range: Float = 60f
    @JvmField var reload = 120f
    @JvmField var maxCoolDown = 240f
    @JvmField var minPrepareTime = 30f
    @JvmField var heatRate = 0.1f
    @JvmField var shieldExpendMinInterval = ShieldExpandEffectDuration * 0.6f
    @JvmField var uninfectedColor: Color = Color.green
    @JvmField var infectedColor: Color = Color.red
    lateinit var unenergizedTR: TR
    lateinit var shieldTR: TR

    init {
        solid = true
        update = true
        group = BlockGroup.projectors
        hasPower = true
    }

    override fun load() {
        super.load()
        unenergizedTR = this.subA("unenergized")
        shieldTR = this.subA("shield")
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.addRangeInfo<AntiVirusBuild>(100f)
        }
        bars.add<AntiVirusBuild>(R.Bar.CoolDownN) {
            ReverseBar(
                { R.Bar.CoolDown.bundle(it.coolDown.seconds) },
                { R.C.CoolDown },
                {
                    if (it.coolDown > reload)
                        it.coolDown / maxCoolDown
                    else it.coolDown / reload
                }
            )
        }
    }

    override fun canReplace(other: Block) = super.canReplace(other) || other is Virus
    override fun minimapColor(tile: Tile) = Color.green.rgba()
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.drawDashCircle(this, x.toShort(), y.toShort(), range, uninfectedColor)
        Vars.indexer.eachBlock(
            Vars.player.team(),
            WorldU.toDrawXY(this, x),
            WorldU.toDrawXY(this, y),
            range,
            {
                true
            }) {
            G.drawSelected(it, getOtherColor(it))
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

        open fun resetCoolDown() {
            coolDown = reload
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
                resetCoolDown()
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
                    if (absorbBullet(it, this)) {
                        eliminated = true
                        heat()
                    }
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
            if (Mathf.zero(power.status)) {
                Draw.rect(unenergizedTR, x, y)
            }
        }

        override fun drawSelect() {
            if (!Mathf.zero(power.status)) {
                Vars.indexer.eachBlock(this, realRange,
                    {
                        true
                    }) {
                    G.drawSelected(it, getOtherColor(it))
                }
                Drawf.dashCircle(x, y, realRange, uninfectedColor)
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
    }

    companion object {
        @JvmStatic
        fun absorbBullet(bullet: Bullet, build: AntiVirusBuild): Boolean {
            if (bullet.team !== build.team &&
                bullet.type.absorbable &&
                bullet.dst(build) <= build.realRange
            ) {
                bullet.absorb()
                Fx.absorb.at(bullet)
                return true
            }
            return false
        }
    }
}