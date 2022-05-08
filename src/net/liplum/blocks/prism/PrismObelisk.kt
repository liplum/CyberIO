package net.liplum.blocks.prism

import arc.math.Mathf
import arc.struct.EnumSet
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.WhenNotPaused
import net.liplum.blocks.prism.Prism.PrismBuild
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AnimationObj
import net.liplum.lib.animations.anims.pingPong
import net.liplum.lib.bundle
import net.liplum.utils.TE
import net.liplum.utils.autoAnim
import net.liplum.utils.exists

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
        noUpdateDisabled = true
        canOverdrive = false
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
                    if (it.linked != -1)
                        R.Bar.Linked.bundle()
                    else
                        R.Bar.NoLink.bundle()
                }, AutoRGB,
                { if (it.linked != -1) 1f else 0f }
            )
        }
    }

    open inner class ObeliskBuild : Building() {
        var linked: Int = -1
        /**
         * Left->Down->Right->Up
         */
        @JvmField var prismOrient = 0
        @ClientOnly lateinit var BlinkObjs: Array<AnimationObj>

        init {
            ClientOnly {
                BlinkObjs = Array(4) {
                    BlinkAnim.gen().pingPong().apply { sleepInstantly() }
                }
            }
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            val mayLinked = linked
            if (mayLinked != -1 && !mayLinked.TE<PrismBuild>().exists) {
                linked = -1
            }
        }

        open fun canLink(prism: PrismBuild) = prismType == prism.block && linked == -1
        open fun link(prism: PrismBuild) {
            if (canLink(prism)) {
                linked = prism.pos()
            }
        }

        open fun unlink() {
            linked = -1
        }

        override fun draw() {
            super.draw()
            WhenNotPaused {
                val d = delta()
                for ((i, obj) in BlinkObjs.withIndex()) {
                    if (linked == -1) {
                        obj.sleep()
                    } else
                        obj.wakeUp()
                    obj.spend(d + Mathf.random())
                    obj.draw(x, y, i * 90f)
                }
            }
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            linked = read.i()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.i(linked)
        }
    }
}
