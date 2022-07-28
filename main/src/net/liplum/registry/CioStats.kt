package net.liplum.registry

import mindustry.world.meta.Stat
import mindustry.world.meta.StatCat
import net.liplum.R
import net.liplum.annotations.DependOn

object CioStatCats {
    @JvmStatic lateinit var heimdall: StatCat
    @JvmStatic lateinit var dataTransfer: StatCat
    @DependOn
    fun statCats() {
        heimdall = CioStatCat("heimdall")
        dataTransfer = CioStatCat("data-transfer")
    }

    private fun CioStatCat(name: String): StatCat =
        StatCat(R.Gen(name))
}

object CioStats {
    @JvmStatic lateinit var heimdallBase: Stat
    @JvmStatic lateinit var heimdallImprove: Stat
    @JvmStatic lateinit var dataRange: Stat
    @JvmStatic lateinit var dataMaxSender: Stat
    @JvmStatic lateinit var dataMaxReceiver: Stat
    @JvmStatic lateinit var dataMaxHost: Stat
    @JvmStatic lateinit var dataMaxClient: Stat
    @JvmStatic lateinit var powerTransferSpeed: Stat
    @DependOn("CioStatCats.statCats")
    fun stats() {
        heimdallBase = CioStat("basic", CioStatCats.heimdall)
        heimdallImprove = CioStat("improve", CioStatCats.heimdall)
        dataRange = CioStat("range", CioStatCats.dataTransfer)
        dataMaxSender = CioStat("max-sender", CioStatCats.dataTransfer)
        dataMaxReceiver = CioStat("max-receiver", CioStatCats.dataTransfer)
        dataMaxHost = CioStat("max-host", CioStatCats.dataTransfer)
        dataMaxClient = CioStat("max-client", CioStatCats.dataTransfer)
        powerTransferSpeed = CioStat(R.Gen("transfer-speed"), StatCat.power)
    }

    private fun CioStat(name: String, category: StatCat): Stat =
        Stat("${category.name}-$name", category)
}
