package net.liplum.registries

import arc.audio.Sound
import mindustry.gen.Sounds
import net.liplum.utils.LoadSound
import net.liplum.utils.LoadSounds

object CioSounds {
    val EmptySounds = emptyArray<Sound>()
    var tvStatic: Sound = Sounds.none
    var jammerPreShoot: Sound = Sounds.none
    var laserWeak: Array<Sound> = EmptySounds
    var laser: Array<Sound> = EmptySounds
    var laserStrong: Array<Sound> = EmptySounds
    var connected: Sound = Sounds.none
    @JvmStatic
    fun load() {
        tvStatic = "tv-static".LoadSound()
        jammerPreShoot = "jammer-pre-shoot".LoadSound()
        laserWeak = "laser-weak".LoadSounds(3)
        laser = "laser".LoadSounds(3)
        laserStrong = "laser-strong".LoadSounds(3)
        connected = "connected".LoadSound()
    }
}
