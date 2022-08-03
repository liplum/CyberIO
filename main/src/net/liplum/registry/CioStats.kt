package net.liplum.registry

import mindustry.world.meta.Stat
import mindustry.world.meta.StatCat
import net.liplum.R
import net.liplum.cio

object CioStats {
    // Categories
    @JvmStatic val heimdall = CioStatCat("heimdall")
    @JvmStatic val dataTransfer = CioStatCat("data-transfer")
    @JvmStatic val holography = CioStatCat("holography")
    // Items
    @JvmStatic val heimdallBase = heimdall("basic")
    @JvmStatic val heimdallImprove = heimdall("improve")
    @JvmStatic val dataRange = dataTransfer("range")
    @JvmStatic val dataMaxSender = dataTransfer("max-sender")
    @JvmStatic val dataMaxReceiver = dataTransfer("max-receiver")
    @JvmStatic val dataMaxHost = dataTransfer("max-host")
    @JvmStatic val dataMaxClient = dataTransfer("max-client")
    @JvmStatic val dataTransferSpeed = dataTransfer("speed")
    @JvmStatic val powerTransferSpeed = StatCat.power("transfer-speed".cio)
    @JvmStatic val maxObelisk = StatCat.optional("max-obelisk".cio)
    @JvmStatic val holoCharge = holography("charge")
    @JvmStatic val holoHpAtLeast = holography("hp-at-least")
    operator fun StatCat.invoke(name: String) = Stat("${this.name}-$name", this)
    private fun CioStatCat(name: String) = StatCat(R.Gen(name))
}
