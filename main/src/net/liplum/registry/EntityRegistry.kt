package net.liplum.registry

import arc.func.Prov
import arc.struct.ObjectMap
import arc.util.Log
import mindustry.gen.EntityMapping
import mindustry.gen.Entityc
import net.liplum.CLog.log
import net.liplum.Meta
import net.liplum.OnlyDebug
import net.liplum.annotations.SubscribeEvent
import net.liplum.api.cyber.DataNetworkUpdater
import net.liplum.event.CioLoadContentEvent
import net.liplum.holo.HoloUnit
import net.liplum.mdt.IsServer

object EntityRegistry {
    private val Clz2Entry = ObjectMap<Class<*>, ProvEntry>()
    private val Clz2Id = ObjectMap<Class<*>, Int>()
    private fun registerInCio() {
        this[HoloUnit::class.java] = { HoloUnit() }
        //this[MagneticField::class.java] = { MagneticField.create() }
        //this[NpcUnit::class.java] = { NpcUnit() }
        // this[DataNetworkUpdater::class.java] = { DataNetworkUpdater.create() }
    }

    private operator fun <T : Entityc> set(c: Class<T>, p: ProvEntry) {
        Clz2Entry.put(c, p)
    }

    private operator fun <T : Entityc> set(c: Class<T>, prov: Prov<T>) {
        set(c, ProvEntry(c.javaClass.toString(), prov))
    }

    fun <T : Entityc> getID(c: Class<T>): Int {
        return Clz2Id[c]
    }

    operator fun <T : Entityc> get(c: Class<T>): Int = getID(c)
    operator fun <T : Entityc> get(c: T): Int = getID(c.javaClass)
    private fun register(clz: Class<*>) {
        Clz2Id.put(clz, EntityMapping.register(clz.toString(), Clz2Entry[clz].prov))
    }
    @JvmStatic
    @SubscribeEvent(CioLoadContentEvent::class)
    fun registerAll() {
        registerInCio()
        val keys = Clz2Entry.keys().toSeq().sortComparing {
            it.name
        }
        keys.forEach { register(it) }
        (OnlyDebug or IsServer){
            Clz2Id.log("${Meta.Name} Unit") { clz, i ->
                Log.info("${i}|${clz.simpleName}")
            }
        }
    }
}

private class ProvEntry(
    val name: String, val prov: Prov<*>
) {
    constructor(prov: Prov<*>) : this(
        prov.get().javaClass.toString(),
        prov
    )
}
