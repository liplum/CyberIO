package net.liplum.registries

import mindustry.world.meta.Stat
import mindustry.world.meta.StatCat
import net.liplum.annotations.DependOn

object CioStatCats {
    @JvmStatic lateinit var heimdall: StatCat
    @DependOn
    fun statCats() {
        heimdall = StatCat("heimdall")
    }
}

object CioStats {
    @JvmStatic lateinit var heimdallBase: Stat
    @JvmStatic lateinit var heimdallImprove: Stat
    @DependOn("CioStatCats.statCats")
    fun stats() {
        heimdallBase = Stat("heimdall-basic", CioStatCats.heimdall)
        heimdallImprove = Stat("heimdall-improve", CioStatCats.heimdall)
    }
}
