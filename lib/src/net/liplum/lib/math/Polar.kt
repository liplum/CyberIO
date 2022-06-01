package net.liplum.lib.math

import arc.math.Mathf
import arc.math.geom.Vec2
import arc.math.geom.Vec3
import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.lib.persistence.CacheReaderSpec
import net.liplum.lib.persistence.CacheWriter
import net.liplum.lib.persistence.IRWable
import java.io.DataInputStream
import kotlin.math.atan2

/**
 * It represents a polar coordinate using radian.
 */
class Polar(
    @JvmField
    var r: Float = 0f,
    @JvmField
    var a: Float = 0f,
) : IRWable {
    fun fromXY(x: Float, y: Float): Polar {
        r = Mathf.sqrt(x * x + y * y)
        a = atan2(y.toDouble(), x.toDouble()).toFloat()
        return this
    }

    fun fromV2d(v2d: Vec2): Polar {
        fromXY(v2d.x, v2d.y)
        return this
    }

    val x: Float
        get() = r * Mathf.cos(a)
    val y: Float
        get() = r * Mathf.sin(a)
    val v2d: Vec2
        get() = Vec2(x, y)
    val v3d: Vec3
        get() = Vec3(x.toDouble(), y.toDouble(), 1.0)

    companion object {
        @JvmStatic
        fun toR(x: Float, y: Float): Float {
            return Mathf.sqrt(x * x + y * y)
        }
        @JvmStatic
        fun toA(x: Float, y: Float): Float {
            return atan2(y.toDouble(), x.toDouble()).toFloat()
        }
        @JvmStatic
        fun byXY(x: Float, y: Float): Polar {
            return Polar().fromXY(x, y)
        }
        @JvmStatic
        fun byV2d(v2d: Vec2): Polar {
            return Polar().fromV2d(v2d)
        }
    }

    override fun read(reader: Reads) {
        r = reader.f()
        a = reader.f()
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        r = f()
        a = f()
    }

    override fun write(writer: Writes) {
        writer.f(r)
        writer.f(a)
    }

    override fun write(writer: CacheWriter) {
        writer.f(r)
        writer.f(a)
    }
}
