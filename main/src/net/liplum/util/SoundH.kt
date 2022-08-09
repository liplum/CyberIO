package net.liplum.util

import arc.audio.Sound
import mindustry.Vars
import net.liplum.R
import net.liplum.mdt.utils.EmptySound
import net.liplum.mdt.utils.loadSound

fun loadOgg(name: String): Sound =
    loadSound(R.Sound.OGG(name))

fun loadWav(name: String): Sound =
    loadSound(R.Sound.WAV(name))

fun loadMp3(name: String): Sound =
    loadSound(R.Sound.MP3(name))

fun loadAuto(name: String): Sound {
    var found: String? = null
    for (extension in R.Sound.extensions) {
        val path = R.Sound.Gen(name, extension)
        if (Vars.tree.get(path).exists()) {
            found = path
        }
    }
    if (found != null) {
        return loadSound(found)
    }
    return EmptySound
}

fun String.LoadSound(): Sound =
    loadAuto(this)

fun String.LoadSounds(number: Int): Array<Sound> =
    Array(number) {
        loadAuto(this + it)
    }
