package net.liplum.registry

import arc.func.Prov
import arc.struct.ObjectMap
import mindustry.gen.EntityMapping
import mindustry.gen.Entityc
import net.liplum.annotations.SubscribeEvent
import net.liplum.api.cyber.DataNetworkUpdater
import net.liplum.event.CioLoadContentEvent
import net.liplum.holo.HoloUnit

/**
 * For entity register.
 * Because Mindustry forbade mod's [ClassLoader] to access parent,
 * so this class won't be shared among different mods.
 */
object EntityRegistry {
    data class ProvEntry(val name: String, val prov: Prov<*>)

    val Clz2Entry = ObjectMap<Class<*>, ProvEntry>()
    val Clz2Id = ObjectMap<Class<*>, Int>()
    fun <T> register(clz: Class<T>, prov: Prov<T>) where T : Entityc {
        Clz2Entry.put(clz, ProvEntry(clz.name, prov))
        Clz2Id.put(clz, EntityMapping.register(clz.name, prov))
    }

    inline fun <reified T : Entityc> register(prov: Prov<T>) {
        register(T::class.java, prov)
    }

    operator fun <T : Entityc> set(clz: Class<T>, prov: Prov<T>) {
        register(clz, prov)
    }

    operator fun <T : Entityc> get(c: Class<T>): Int = Clz2Id[c]
    inline fun <reified T : Entityc> getIdOf(): Int = Clz2Id[T::class.java]
}

object CioEntity {
    @JvmStatic
    @SubscribeEvent(CioLoadContentEvent::class)
    fun registerAll() {
        EntityRegistry.apply {
            register<HoloUnit>(::HoloUnit)
            register<DataNetworkUpdater>(DataNetworkUpdater::create)
            //this[MagneticField::class.java] = { MagneticField.create() }
            //this[NpcUnit::class.java] = { NpcUnit() }
        }
    }
}
