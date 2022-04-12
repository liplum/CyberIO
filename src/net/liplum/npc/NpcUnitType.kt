package net.liplum.npc

import mindustry.type.UnitType

open class NpcUnitType(name: String) : UnitType(name) {
    override fun unlock() {}
    override fun unlocked()=false
    override fun unlockedNowHost()=false
    override fun unlockedNow()=false
}