package net.liplum.blocks.prism

import arc.struct.EnumSet
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.animations.anims.Animation
import net.liplum.animations.anims.AnimationObj
import net.liplum.animations.anims.pingPong
import net.liplum.blocks.prism.Prism.PrismBuild
import net.liplum.utils.autoAnim
import net.liplum.utils.bundle

open class PrismObelisk(name: String) : Block(name) {
    @JvmField var prismType: Prism? = null
    lateinit var BlinkAnim: Animation
    @JvmField var BlinkFrames = 6
    @JvmField var BlinkDuration = 20f

    init {
        absorbLasers = true
        update = true
        solid = true
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
    }

    override fun load() {
        super.load()
        BlinkAnim = this.autoAnim("blink", BlinkFrames, BlinkDuration)
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

    open inner class ObeliskBuild : Building() {
        @JvmField var linkedPos = -1
        var linked: PrismBuild? = null
        /**
         * Left->Down->Right->Up
         */
        @JvmField var prismOrient = 0
        @ClientOnly lateinit var BlinkObjs: Array<AnimationObj>
        override fun onProximityUpdate() {
            super.onProximityUpdate()
            val mayLinked = linked
            if (mayLinked != null && mayLinked.tile.build != mayLinked) {
                linked = null
            }
        }

        override fun onProximityAdded() {
            super.onProximityAdded()
            linked = Vars.world.build(linkedPos) as? PrismBuild
        }

        override fun created() {
            super.created()
            ClientOnly {
                BlinkObjs = Array(4) {
                    BlinkAnim.gen().pingPong().apply { sleepInstantly() }
                }
            }
        }

        open fun canLink(prism: PrismBuild) = prismType == prism.block && linked == null
        override fun draw() {
            super.draw()
            val d = delta()
            for ((i, obj) in BlinkObjs.withIndex()) {
                if (linked == null) {
                    obj.sleep()
                } else
                    obj.wakeUp()
                obj.spend(d)
                obj.draw(x, y, i * 90f)
            }
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            linkedPos = read.i()
            linked = Vars.world.build(linkedPos) as? PrismBuild
        }

        override fun write(write: Writes) {
            super.write(write)
            write.i(linked?.pos() ?: -1)
        }
    }
}
