package net.liplum.utils

import arc.struct.ObjectMap

operator fun <K, V> ObjectMap.Entry<K, V>.component1(): K {
    return this.key
}

operator fun <K, V> ObjectMap.Entry<K, V>.component2(): V {
    return this.value
}
