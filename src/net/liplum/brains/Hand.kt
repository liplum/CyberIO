package net.liplum.brains

import arc.util.Time
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import net.liplum.ClientOnly
import net.liplum.api.brain.*
import net.liplum.lib.Tx
import net.liplum.lib.skeletal.Bone
import net.liplum.lib.skeletal.Joint
import net.liplum.lib.skeletal.Skeleton
import net.liplum.lib.skeletal.Skin
import net.liplum.utils.MdtUnit
import net.liplum.utils.inMod

open class Hand(name: String) : Block(name), IComponentBlock {
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    @ClientOnly lateinit var BoneTx: Tx
    @ClientOnly lateinit var JointTx: Tx
    var reloadTime = 60f

    init {
        update = true
        solid = true
    }

    override fun load() {
        super.load()
        BoneTx = Tx(this.inMod("bone"))
        JointTx = Tx(this.inMod("joint"))
    }

    open inner class HandBuild : Building(),
        IUpgradeComponent, ControlBlock {
        override var directionInfo: Direction2 = Direction2.Empty
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade> = this@Hand.upgrades
        var unit = UnitTypes.block.create(team) as BlockUnitc
        var lastJoint: Joint
        val skeleton = Skeleton().apply {
            val jointSkin = Skin(JointTx)
            val boneSkin = Skin(BoneTx)
            val sk = this
            root = Joint(sk).apply {
                // ja 1
                skin = jointSkin
                pos.set(0f, 0f)
                setNextBone(Bone(sk).apply {
                    // bone 1
                    skin = boneSkin
                    length = 16f
                    setJb(Joint(sk).apply {
                        // jb 1
                        skin = jointSkin
                        pos.set(0f, 16f)
                        setNextBone(Bone(sk).apply {
                            // bone 2
                            skin = boneSkin
                            length = 64f
                            setJb(Joint(sk).apply {
                                // jb 2
                                skin = jointSkin
                                lastJoint = this
                                pos.set(0f, 16f + 16f)
                            })
                        })
                    })
                })
            }
        }
        var reload = 0f
        override fun updateTile() {
            reload += Time.delta
            if (reload > reloadTime && isControlled && unit.isShooting) {
                reload = 0f
                lastJoint.applyForce(0.1f, 0f)
            }
            skeleton.update(Time.delta * 0.1f)
        }

        override fun draw() {
            super.draw()
            skeleton.drawLinear(x, y, 0f)
        }

        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }
    }
}