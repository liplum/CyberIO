package net.liplum.mdt

import arc.Core
import arc.Events
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import net.liplum.lib.UseReflection
import net.liplum.lib.utils.setFIn

@UseReflection
fun UnlockableContent.lock() {
    val isUnlocked = Core.settings.getBool("$name-unlocked", true)
    if (isUnlocked) {
        Core.settings.put("$name-unlocked", false)
        setFIn(UnlockableContent::class.java, "unlocked", false)
        Vars.state.rules.researched.remove(name)
        Events.fire(LockEvent(this))
        this.techNode?.reset()
    }
}

class LockEvent(val content: UnlockableContent)