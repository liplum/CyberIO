package net.liplum.registries

import arc.func.Prov
import arc.struct.ObjectMap
import arc.util.Log
import mindustry.gen.EntityMapping
import mindustry.gen.Entityc
import net.liplum.Clog.log
import net.liplum.Meta
import net.liplum.OnlyDebug
import net.liplum.OnlyServer
import net.liplum.holo.HoloUnit
import net.liplum.npc.NpcUnit

object EntityRegistry {
    //start from 100
    private val Clz2Entry = ObjectMap<Class<*>, ProvEntry>()
    val Clz2Id = ObjectMap<Class<*>, Int>()

    init {
        this[HoloUnit::class.java] = { HoloUnit() }
        this[NpcUnit::class.java] = { NpcUnit() }
    }

    operator fun <T : Entityc> set(c: Class<T>, p: ProvEntry) {
        Clz2Entry.put(c, p)
    }

    operator fun <T : Entityc> set(c: Class<T>, prov: Prov<T>) {
        set(c, ProvEntry(c.javaClass.toString(), prov))
    }

    fun <T : Entityc> getID(c: Class<T>): Int {
        return Clz2Id[c]
    }

    fun register(clz: Class<*>) {
        Clz2Id.put(clz, EntityMapping.register(clz.toString(), Clz2Entry[clz].prov))
    }
    @JvmStatic
    fun registerAll() {
        val keys = Clz2Entry.keys().toSeq().sortComparing {
            it.name
        }
        keys.forEach { register(it) }
        (OnlyDebug or OnlyServer){
            Clz2Id.log("${Meta.Name} Unit") { clz, i ->
                Log.info("$i|${clz.simpleName}")
            }
        }
    }
}

class ProvEntry(
    val name: String, val prov: Prov<*>
) {
    constructor(prov: Prov<*>) : this(
        prov.get().javaClass.toString(),
        prov
    )
}
