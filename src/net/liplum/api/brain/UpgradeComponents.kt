package net.liplum.api.brain

import arc.util.Log
import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.ClientOnly
import net.liplum.api.brain.Direction2.Companion.Part0Pos
import net.liplum.api.brain.Direction2.Companion.Part1Pos
import net.liplum.api.cyber.*
import net.liplum.lib.delegates.Delegate
import net.liplum.utils.*

@JvmInline
value class UpgradeType(val type: Int) {
    companion object {
        val Damage = UpgradeType(0)
        val Range = UpgradeType(1)
        val WaveSpeed = UpgradeType(2)
        val WaveWidth = UpgradeType(3)
        val ReloadTime = UpgradeType(4)
        /**
         * To prevent radiating too many waves, it doesn't mean the absolute value will be used.
         */
        val MaxBrainWaveNum = UpgradeType(5)
    }
}

data class Upgrade(val type: UpgradeType, val isDelta: Boolean, val value: Float)
interface IUpgradeComponent : ICyberEntity {
    var directionInfo: Direction2
    var brain: IBrain?
    val isLinkedBrain: Boolean
        get() = brain != null
    val upgrades: Map<UpgradeType, Upgrade>
    fun canLinked(brain: IBrain): Boolean =
        !isLinkedBrain

    fun linkBrain(brain: IBrain, dire: Direction2): Boolean {
        if (canLinked(brain)) {
            this.brain = brain
            directionInfo = dire
            return true
        }
        return false
    }

    fun unlinkBrain() {
        brain = null
        directionInfo = Direction2.Empty
    }

    fun clear() {
        unlinkBrain()
    }
}

interface IComponentBlock {
    val upgrades: MutableMap<UpgradeType, Upgrade>
    fun addUpgrade(upgrade: Upgrade) {
        upgrades[upgrade.type] = upgrade
    }

    fun addUpgrade(vararg upgrades: Upgrade) {
        for (upgrade in upgrades)
            addUpgrade(upgrade)
    }

    fun checkInit() {
        this as Block
        if (!(size == 2 || size == 4)) {
            Log.warn("Block $name's size isn't 2 or 4 but $size, so it was set as 2 automatically.")
            size = 2
        }
    }
}
/**
 * For 2 part: 0 and 1
 */
class Side2(val brain: IBrain) : Iterable<IUpgradeComponent> {
    val components: Array<IUpgradeComponent?> = arrayOfNulls(2)
    val occupyByOne: Boolean
        get() = components[0] != null && components[0] == components[1]

    operator fun get(pos: Int): IUpgradeComponent? =
        components[pos]

    operator fun set(pos: Int, component: IUpgradeComponent?) {
        val original = components[pos]
        var changed = false
        if (original != null) {
            brain.components.remove(original)
            changed = true
        }
        components[pos] = component
        if (component != null) {
            brain.components.add(component)
            changed = true
        }
        if (changed)
            brain.onComponentChanged()
    }

    fun statsTrueCount(): Int {
        val p0 = components[0]
        val p1 = components[1]
        if (p0 == p1) {
            return if (p0 == null) 0 else 1
        }
        if (components[0] == null || components[1] == null) return 1
        return 2
    }

    inline fun find(filter: (IUpgradeComponent) -> Boolean): IUpgradeComponent? {
        for (c in components)
            if (c != null && filter(c))
                return c
        return null
    }

    override fun iterator(): Iterator<IUpgradeComponent> {
        return components.filterNotNull().iterator()
    }

    fun clear() {
        components.fill(null)
    }

    fun unlink(){
       for (com in components)
           com?.unlinkBrain()
    }
}
/**
 * For 4 sides: top, bottom, left and right
 * For 2 part: 0 and 1
 */
@JvmInline
value class Direction2(val value: Int = -1) {
    companion object {
        val Empty: Direction2 = Direction2()
        const val Part0Pos = 2
        const val Part1Pos = 3
    }

    val isClinging: Boolean
        get() = value != -1 && (onPart0 || onPart1)
    val side: Int
        get() = value and 3
    val isRight: Boolean
        get() = side == 0
    val isTop: Boolean
        get() = side == 1
    val isLeft: Boolean
        get() = side == 2
    val isBottom: Boolean
        get() = side == 3
    val onPart0: Boolean
        get() = value isOn 2
    val onPart1: Boolean
        get() = value isOn 3
    val occupySide: Boolean
        get() = onPart0 && onPart1
}

interface IBrain : ICyberEntity, Iterable<IUpgradeComponent> {
    /**
     * 4 sides of this brain block.
     * - 0 is [right]
     * - 1 is [top]
     * - 2 is [left]
     * - 3 is [bottom]
     */
    val sides: Array<Side2>
    val components: MutableSet<IUpgradeComponent>
    val onComponentChanged: Delegate
    val right: Side2
        get() = sides[0]
    val top: Side2
        get() = sides[1]
    val left: Side2
        get() = sides[2]
    val bottom: Side2
        get() = sides[3]
    val Direction2.sideObj: Side2
        get() = sides[this.side]

    fun clear() {
        for (side in sides)
            side.clear()
        components.clear()
    }

    fun unlinkAll(){
        for (side in sides)
            side.unlink()
    }
    /**
     * ## Contract
     * 1. This tile entity's size is 4
     * 2. The return value has 4 bits
     * ### For side (2 bits)
     * - 0 is [right]
     * - 1 is [top]
     * - 2 is [left]
     * - 3 is [bottom]
     * ### For part (2 bits)
     * - 0 is Up/Left
     * - 1 is Down/Right
     * @return the direction of [b] relative to this tile entity.
     */
    @Suppress("KotlinConstantConditions")
    fun sideOn(b: Building): Direction2 {
        val side = building.relativeTo(b).toInt()
        var res = side
        when (side) {
            //right
            0 -> {
                if (topRightX + 1 == b.topLeftX && topRightY == b.topLeftY)
                    res = res on Part0Pos
                if (bottomRightX + 1 == b.bottomLeftX && bottomRightY == b.bottomLeftY)
                    res = res on Part1Pos
            }
            // top
            1 -> {
                if (topLeftX == b.bottomLeftX && topLeftY + 1 == b.bottomLeftY)
                    res = res on Part0Pos
                if (topRightX == b.bottomRightX && topRightY + 1 == b.bottomRightY)
                    res = res on Part1Pos
            }
            // left
            2 -> {
                if (topLeftX - 1 == b.topRightX && topLeftY == b.topRightY)
                    res = res on Part0Pos
                if (bottomLeftX - 1 == b.bottomRightX && bottomLeftY == b.bottomRightY)
                    res = res on Part1Pos
            }
            // bottom
            3 -> {
                if (bottomLeftX == b.topLeftX && bottomLeftY - 1 == b.topLeftY)
                    res = res on Part0Pos
                if (bottomRightX == b.topRightX && bottomRightY - 1 == b.topRightY)
                    res = res on Part1Pos
            }
        }
        return Direction2(res)
    }

    override fun iterator() = components.iterator()

    companion object {
        inline fun IBrain.find(filter: (IUpgradeComponent) -> Boolean): IUpgradeComponent? {
            for (c in components)
                if (filter(c))
                    return c
            return null
        }
        @ClientOnly
        val XSign = arrayOf(+1, -1, -1, -1)
        @ClientOnly
        val YSign = arrayOf(+1, +1, +1, -1)
        @ClientOnly
        val Mirror = arrayOf(+1, +1, -1, -1)
    }
}
