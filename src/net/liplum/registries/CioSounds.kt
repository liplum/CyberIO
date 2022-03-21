package net.liplum.registries

import arc.audio.Sound
import net.liplum.utils.LoadSound

object CioSounds {
    lateinit var tvStatic: Sound
    lateinit var jammerPreShoot: Sound
    @JvmStatic
    fun load() {
        tvStatic = "tv-static".LoadSound()
        jammerPreShoot = "jammer-pre-shoot".LoadSound()
    }
}
