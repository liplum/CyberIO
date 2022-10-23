package net.liplum.registry

import arc.audio.Sound
import mindustry.gen.Sounds
import net.liplum.annotations.SubscribeEvent
import net.liplum.event.CioLoadContentEvent
import net.liplum.utils.LoadSound
import net.liplum.utils.LoadSounds
import plumy.core.assets.EmptySounds

object CioSound {
    @JvmField var tvStatic: Sound = Sounds.none
    @JvmField var jammerPreShoot: Sound = Sounds.none
    @JvmField var laserWeak: Array<Sound> = EmptySounds
    @JvmField var laser: Array<Sound> = EmptySounds
    @JvmField var laserStrong: Array<Sound> = EmptySounds
    @JvmField var crystal: Array<Sound> = EmptySounds
    @JvmField var connected: Sound = Sounds.none
    @JvmField var heartbeat: Sound = Sounds.none
    @JvmField var heartbeatFaster: Sound = Sounds.none
    @JvmStatic
    @SubscribeEvent(CioLoadContentEvent::class)
    fun load() {
        tvStatic = "tv-static".LoadSound()
        jammerPreShoot = "jammer-pre-shoot".LoadSound()
        laserWeak = "laser-weak".LoadSounds(3)
        laser = "laser".LoadSounds(3)
        laserStrong = "laser-strong".LoadSounds(3)
        connected = "connected".LoadSound()
        heartbeat = "heartbeat".LoadSound()
        heartbeatFaster = "heartbeat-faster".LoadSound()
        crystal = "crystal".LoadSounds(4)
    }
}
