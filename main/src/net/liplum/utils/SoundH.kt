package net.liplum.utils

import arc.Core
import arc.assets.loaders.SoundLoader
import arc.audio.Sound
import arc.func.Cons
import mindustry.gen.Sounds
import plumy.core.ClientOnly

val EmptySound: Sound = Sounds.none
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