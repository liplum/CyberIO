package net.liplum.mdt.utils

import arc.Core
import arc.assets.loaders.SoundLoader
import arc.audio.Sound
import arc.func.Cons
import net.liplum.mdt.ClientOnly

val EmptySound: Sound by lazy {
    Sound()
}

fun loadSound(path: String): Sound {
    ClientOnly {
        val sound = Sound()
        Core.assets.load(
            path, Sound::class.java, SoundLoader.SoundParameter(sound)
        ).errored = Cons {
            it.printStackTrace()
        }
        return sound
    }
    return EmptySound
}