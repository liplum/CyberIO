package net.liplum.lib

import arc.Core
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class Setting<Owner, T>(
    val key: String, val default: T, val mapping: Setting<Owner, T>.(String) -> Unit = {}
) : ReadWriteProperty<Owner, T>, PropertyDelegateProvider<Owner, Setting<Owner, T>> {
    override fun provideDelegate(thisRef: Owner, property: KProperty<*>): Setting<Owner, T> {
        mapping(property.name)
        return this
    }

    override fun getValue(thisRef: Owner, property: KProperty<*>): T =
        Core.settings.get(property.name, default) as? T ?: default

    operator fun get(key: String): T =
        Core.settings.get(key, default) as? T ?: default

    override fun setValue(thisRef: Owner, property: KProperty<*>, value: T) {
        Core.settings.put(property.name, value)
    }

    operator fun set(key: String, value: T) {
        Core.settings.put(key, value)
    }
}