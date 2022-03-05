package net.liplum.blocks.prism

import arc.struct.EnumSet
import arc.util.Time
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.R
import net.liplum.blocks.prism.Prism.PrismBuild
import net.liplum.utils.bundle

class PrismObelisk(name: String) : Block(name) {
    @JvmField var prismType: Prism? = null

    init {
        absorbLasers = true
        update = true
        solid = true
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
    }

    override fun setBars() {
        super.setBars()
        bars.add<ObeliskBuild>(R.Bar.LinkedN) {
            Bar(
                {
                    if (it.linked != null)
                        R.Bar.Linked.bundle()
                    else
                        R.Bar.NoLink.bundle()
                },
                {
                    val rgb = R.C.PrismRgbFG
                    val len = rgb.size
                    val total = len * 60f
                    rgb[((Time.time % total / total) * len).toInt().coerceIn(0, len - 1)]
                },
                { if (it.linked != null) 1f else 0f }
            )
        }
    }

    inner class ObeliskBuild : Building() {
        var linked: PrismBuild? = null
        val prismType: Prism
            get() = this@PrismObelisk.prismType!!

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            val mayLinked = linked
            if (mayLinked != null && mayLinked.tile.build != mayLinked) {
                linked = null
            }
        }
    }
}
