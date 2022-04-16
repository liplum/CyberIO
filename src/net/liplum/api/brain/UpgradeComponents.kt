package net.liplum.api.brain

import mindustry.gen.Building
import net.liplum.api.brain.Direction4.Companion.Part0Pos
import net.liplum.api.brain.Direction4.Companion.Part1Pos
import net.liplum.api.cyber.*
import net.liplum.utils.*

interface IUpgradeComponent : ICyberEntity {
    var directionInfo: Direction4
    var brain: IBrain?
    val isLinkedBrain: Boolean
        get() = brain != null

    fun canLinked(brain: IBrain): Boolean =
        !isLinkedBrain

    fun linkBrain(brain: IBrain, dire: Direction4) {
        if (canLinked(brain)) {
            this.brain = brain
            directionInfo = dire
        }
    }

    fun unlinkBrain() {
        brain = null
        directionInfo = Direction4.Empty
    }

    fun clear() {
        unlinkBrain()
    }
}

class Side(val brain: IBrain, val size: Int) : Iterable<IUpgradeComponent> {
    val components: Array<IUpgradeComponent?> = arrayOfNulls(size)
    operator fun get(pos: Int): IUpgradeComponent? =
        components[pos]

    operator fun set(pos: Int, component: IUpgradeComponent?) {
        val original = components[pos]
        if (original != null)
            brain.components.remove(original)
        components[pos] = component
        if (component != null)
            brain.components.add(component)
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
}
@JvmInline
value class Direction4(val value: Int = -1) {
    companion object {
        val Empty: Direction4 = Direction4()
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
    val sides: Array<Side>
    val components: MutableSet<IUpgradeComponent>
    val right: Side
        get() = sides[0]
    val top: Side
        get() = sides[1]
    val left: Side
        get() = sides[2]
    val bottom: Side
        get() = sides[3]
    val Direction4.sideObj: Side
        get() = sides[this.side]

    fun clear() {
        for (side in sides)
            side.clear()
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
    fun sideOn(b: Building): Direction4 {
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
                if (bottomLeftX - 1 == b.bottomLeftX && bottomLeftY == b.bottomRightY)
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
        return Direction4(res)
    }

    override fun iterator() = components.iterator()

    companion object {
        inline fun IBrain.find(filter: (IUpgradeComponent) -> Boolean): IUpgradeComponent? {
            for (c in components)
                if (filter(c))
                    return c
            return null
        }
    }
}
