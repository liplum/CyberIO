package net.liplum.npc

import mindustry.gen.UnitEntity
import net.liplum.registries.EntityRegistry

open class NpcUnit : UnitEntity(){
    override fun classId(): Int {
        return EntityRegistry.getID(javaClass)
    }
}