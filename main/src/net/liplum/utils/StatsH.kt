package net.liplum.utils

import arc.scene.ui.Label
import arc.struct.OrderedMap
import arc.struct.Seq
import mindustry.world.Block
import mindustry.world.meta.Stat
import mindustry.world.meta.StatCat
import mindustry.world.meta.StatValue
import net.liplum.mdt.UseReflection
import net.liplum.lib.utils.bundle
import net.liplum.lib.utils.getF

@UseReflection
fun Block.addPowerUseStats() {
    val map = stats.getF<OrderedMap<StatCat, OrderedMap<Stat, Seq<StatValue>>>?>("map")
    if (map != null && map.get(StatCat.power)?.containsKey(Stat.powerUse) == true) {
        stats.remove(Stat.powerUse)
    }
    stats.add(Stat.powerUse) {
        val l = Label("$contentType.$name.stats.power-use".bundle)
        it.add(l)
    }
}