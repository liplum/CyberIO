package net.liplum.api.brain

import arc.util.Log
import arc.util.Time
import mindustry.Vars
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.meta.StatUnit.*
import net.liplum.mdt.ClientOnly
import net.liplum.api.brain.Direction2.Companion.Part0Pos
import net.liplum.api.brain.Direction2.Companion.Part1Pos
import net.liplum.api.cyber.*
import net.liplum.lib.delegates.Delegate
import net.liplum.lib.utils.isOn
import net.liplum.lib.utils.on
import net.liplum.utils.percent
import net.liplum.utils.value
import net.liplum.mdt.utils.*
import kotlin.math.absoluteValue

interface IUpgradeComponent : IHeimdallEntity {
    var directionInfo: Direction2
    val isLinkedBrain: Boolean
        get() = brain != null
    val upgrades: Map<UpgradeType, Upgrade>
    fun canLinked(brain: IBrain): Boolean =
        !isLinkedBrain

    fun linkBrain(brain: IBrain, dire: Direction2): Boolean {
        if (canLinked(brain)) {
            this.brain = brain
            directionInfo = dire
            onLinkedBrain()
            return true
        }
        return false
    }

    fun onLinkedBrain() {
    }

    fun onUnlikedBrain() {
    }

    fun unlinkBrain() {
        if (brain != null) {
            brain = null
            directionInfo = Direction2.Empty
            onUnlikedBrain()
        }
    }

    fun clear() {
        unlinkBrain()
    }
}
/**
 * Iterate other linked parts and run a closure on them.
 * Brain first.
 * If this doesn't link with a brain, nothing will happen
 */
inline fun IUpgradeComponent.onOtherParts(func: IHeimdallEntity.() -> Unit) {
    val brain = brain
    if (brain != null) {
        brain.func()
        for (component in brain) {
            if (component != this)
                component.func()
        }
    }
}
/**
 * Iterate other linked parts and run a closure on them.
 * Brain first.
 * If this doesn't link with a brain, nothing will happen
 */
inline fun IUpgradeComponent.onAllParts(func: IHeimdallEntity.() -> Unit) {
    val brain = brain
    if (brain != null) {
        brain.func()
        for (component in brain) {
            component.func()
        }
    }
}
/**
 * Iterate other linked upgrade components and run a closure on them.
 * If this doesn't link with a brain, nothing will happen
 */
inline fun IUpgradeComponent.onOtherComponents(func: IUpgradeComponent.() -> Unit) {
    val brain = brain
    if (brain != null) {
        for (component in brain) {
            if (component != this)
                component.func()
        }
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
 * For 2 part: 0 and 1.
 * It should be used as an array to represent 4 sides.
 * - 0 is right
 * - 1 is top
 * - 2 is left
 * - 3 is bottom
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

    fun unlinkAll() {
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

internal val Array<Side2>.right: Side2
    get() = this[0]
internal val Array<Side2>.top: Side2
    get() = this[1]
internal val Array<Side2>.left: Side2
    get() = this[2]
internal val Array<Side2>.bottom: Side2
    get() = this[3]
