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
import net.liplum.utils.*

internal const val D2SD = 5f / 6f * Mathf.pi
internal const val HalfPi = 1f / 2f * Mathf.pi
internal const val Left = HalfPi - 0.5f
internal const val Right = HalfPi + 0.5f
internal const val VirusEliminatePct = Left / D2SD
internal var SinLeft = Mathf.sin(HalfPi + 0.5f)
internal fun D2S(pct: Float): Float {
    val x = pct * D2SD
    if (x in Left..Right) {
        return SinLeft
    }
    if (pct in 0f..1f) {
        return Mathf.sin(x)
    }
    return Mathf.sin(D2SD)
}

internal const val ShieldExpandEffectDuration = 70f
internal const val ShieldExpandingDuration = 60f
internal const val VirusEliminatePoint = VirusEliminatePct * ShieldExpandingDuration
val ShieldExpand: Effect = Effect(ShieldExpandEffectDuration) {
    val avb = it.data as AntiVirus.AntiVirusBuild
    val av = avb.block as AntiVirus
    val s = av.shieldTR
    val d2s = D2S(it.time / ShieldExpandingDuration)
    Draw.alpha(it.fout())
    val scale = avb.realRange / 15f
    Draw.rect(
        s, it.x, it.y,
        G.Dx(s) * d2s * scale,
        G.Dy(s) * d2s * scale
    )
}.layer(Layer.power)

fun AntiVirus.AntiVirusBuild.shieldExpanding() {
    ShieldExpand.at(x, y, realRange, this)
}

open class AntiVirus(name: String) : Block(name) {
    var range: Float = 80f
    var reload = 120f
    var shieldExpendMinInterval = 30f
    var uninfectedColor: Color = Color.green
    var infectedColor: Color = Color.red
    lateinit var unenergizedTR: TR
    lateinit var shieldTR: TR
    fun absorbBullet(bullet: Bullet, build: AntiVirusBuild) {
        if (bullet.team !== build.team &&
            bullet.type.absorbable &&
            bullet.dst(build) <= build.realRange
        ) {
            bullet.absorb()
            Fx.absorb.at(bullet)
        }
    }

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
    }

    override fun canReplace(other: Block) = super.canReplace(other) || other is Virus
    override fun minimapColor(tile: Tile) = Color.green.rgba()
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.drawDashCircle(this, x, y, range, uninfectedColor)
        Vars.indexer.eachBlock(
            Vars.player.team(),
            WorldUtil.toDrawXY(this, x),
            WorldUtil.toDrawXY(this, y),
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
            get() = range * efficiency() * timeScale
        open var cleanCharge = 0f
        open var shieldExpendCharge = 0f
        override fun updateTile() {
            cleanCharge += edelta()
            ClientOnly {
                shieldExpendCharge += Time.delta
            }
            if (cleanCharge >= reload) {
                cleanCharge = 0f
                var eliminated = false
                Vars.indexer.eachBlock(this, realRange,
                    { b ->
                        b is IVirusBuilding
                    }) {
                    (it as? IVirusBuilding)?.killVirus()
                    ClientOnly {
                        Time.runTask(VirusEliminatePoint) {
                            Fx.breakBlock.at(it)
                        }
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
                    absorbBullet(it, this)
                    eliminated = true
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
            cleanCharge = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.f(cleanCharge)
        }
    }
}