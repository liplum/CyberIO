package net.liplum.common.util

import arc.struct.ObjectMap
import arc.util.serialization.Json
import arc.util.serialization.JsonValue

operator fun <K, V> ObjectMap.Entry<K, V>.component1(): K {
    return this.key
}

operator fun <K, V> ObjectMap.Entry<K, V>.component2(): V {
    return this.value
}
/**
 * Only allow string, float, int , boolean ang their corresponding array.
 * Otherwise, a null will be returned.
 *
 * Because of the limitation of [Json], the name of an array has to specify its type.
 * such as:
 * - \[S] -> string array
 * - \[I] -> int array
 * - \[F] -> float array
 * - \[B] -> boolean array
 */
fun JsonValue.getValue(): Any? {
    return when (type()) {
        JsonValue.ValueType.array -> {
            if (name.endsWith("[S]"))
                asStringArray()
            if (name.endsWith("[F]"))
                asFloatArray()
            if (name.endsWith("[I]"))
                asIntArray()
            if (name.endsWith("[B]"))
                asBooleanArray()
            null
        }
        JsonValue.ValueType.stringValue -> asString()
        JsonValue.ValueType.doubleValue -> asFloat()
        JsonValue.ValueType.longValue -> asInt()
        JsonValue.ValueType.booleanValue -> asBoolean()
        JsonValue.ValueType.nullValue -> null
        else -> null
    }
}