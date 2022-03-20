package net.liplum.registries

import arc.Core
import arc.assets.loaders.SoundLoader.SoundParameter
import arc.audio.Sound
import arc.func.Cons
import net.liplum.ClientOnly
import net.liplum.R

object CioSounds {
    lateinit var tvStatic: Sound
    lateinit var jammerPreShoot: Sound
    @JvmStatic
    fun load() {
        tvStatic = loadWav("tv-static")
        jammerPreShoot = loadWav("jammer-pre-shoot")
    }
}

val EmptySound: Sound by lazy {
    Sound()
}

fun loadOgg(name: String): Sound =
    loadSound(R.Sound.OGG(name))

fun loadWav(name: String): Sound =
    loadSound(R.Sound.WAV(name))

fun loadSound(path: String): Sound {
    ClientOnly {
        val sound = Sound()
        Core.assets.load(
            path, Sound::class.java, SoundParameter(sound)
        ).errored = Cons {
            it.printStackTrace()
        }
        return sound
    }
    return EmptySound
}