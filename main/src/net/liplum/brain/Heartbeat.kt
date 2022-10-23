package net.liplum.brain

import arc.audio.Sound
import arc.graphics.Color
import mindustry.gen.Sounds
import mindustry.type.Liquid
import mindustry.world.meta.Attribute
import net.liplum.R

class Heartbeat {
    @JvmField var shake = FProp()
    @JvmField var reloadTime = FProp(Decrease)
    @JvmField var powerUse = FProp()
    @JvmField var damage = FProp()
    @JvmField var bloodCost = FProp()
    @JvmField var range = FProp()
    @JvmField var shootNumber = IntProp()
    // 200 ticks -> 165 range
    // 300 ticks -> 240 range
    @JvmField var bulletLifeTime = FProp()
    // Sound
    @JvmField var soundGetter: (Float) -> Sound = { Sounds.none }
    // Systole&Diastole
    // normal: 3.5f ,improved: 3.3f
    @JvmField var diastole = FProp(Decrease)
    @JvmField var systoleMinIn = 0.02f
    @JvmField var systoleTime = 1f
    @JvmField var offset = 0f
    /**
     * The force of systole
     */
    // normal: 0.192f, improved: 0.175f
    @JvmField var systole = FProp()
}
internal typealias Monotone = Boolean

internal const val Increase = true
internal const val Decrease = false

class FProp(
    val isIncrease: Monotone = Increase,
) {
    @JvmField var base = 0f
    @JvmField var downRange = 0f
    @JvmField var upRange = 0f
    inline fun config(func: FProp.() -> Unit) {
        func()
    }

    fun limitIn(min: Float, max: Float): FProp {
        base = min
        upRange = max - min
        downRange = 0f
        return this
    }

    fun setRange(v: Float): FProp {
        downRange = v
        upRange = v
        return this
    }
    /**
     * @param p belongs to [-1,1]
     */
    fun progress(p: Float): Float {
        val delta = if (isIncrease) {
            //if (p > 0f) p * upRange else -p.absoluteValue * downRange
            if (p > 0f) p * upRange else p * downRange
        } else {
            //if (p > 0f) -p * downRange else p.absoluteValue * upRange
            if (p > 0f) -p * downRange else -p * upRange
        }
        return (base + delta).coerceAtLeast(0f)
    }
}

class IntProp(
    val isIncrease: Monotone = Increase,
) {
    @JvmField var base = 0
    @JvmField var downRange = 0
    @JvmField var upRange = 0
    inline fun config(func: IntProp.() -> Unit) {
        func()
    }

    fun limitIn(min: Int, max: Int): IntProp {
        base = min
        upRange = max - min
        downRange = 0
        return this
    }

    fun setRange(v: Int): IntProp {
        downRange = v
        upRange = v
        return this
    }
    /**
     * @param p belongs to [-1,1]
     */
    fun progress(p: Float): Int {
        val delta = if (isIncrease) {
            //if (p > 0f) p * upRange else -p.absoluteValue * downRange
            if (p > 0f) p * upRange else p * downRange
        } else {
            //if (p > 0f) -p * downRange else p.absoluteValue * upRange
            if (p > 0f) -p * downRange else -p * upRange
        }
        return (base + delta).coerceAtLeast(0f).toInt()
    }
}
/**
 * Copy from [Liquid]
 */
class Blood {
    /**
     * Water is 0.5f
     */
    var temperature = 0.5001f
    /**
     * Water: 4182--->0.4
     * Blood: 3617--->0.3007
     */
    var heatCapacity = 0.3007f
    /**
     * Blood's boil point is a bit less than water because of salt.
     * Water's 0.5f --> 100 C
     */
    var boilPoint = 0.499f
    var flammability = 0f
    var color: Color = R.C.Blood
    var gasColor: Color = Color.lightGray.cpy()
    /** @return true if blood will boil in this global environment.
     */
    fun willBoil(): Boolean {
        return Attribute.heat.env() >= boilPoint
    }

    fun canExtinguish(): Boolean {
        return flammability < 0.1f && temperature <= 0.5f
    }

    fun calcuEnthalpy(amount: Float): Float {
        return amount * heatCapacity * temperature
    }

    companion object {
        val X = Blood()
    }
}
/**
 *  enthalpy = mass * capacity * temperature
 *  H = m C T
 * @param amount Assume it's mass
 */
fun Liquid.calcEnthalpy(amount: Float): Float {
    return amount * heatCapacity * temperature
}