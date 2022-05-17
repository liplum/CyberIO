package net.liplum

object Debug {
    var enableUnlockContent = false
    var settings = listOf(
        Setting(
            "Enable Unlock Content",
            ::enableUnlockContent::get,
            ::enableUnlockContent::set,
        )
    )

    class Setting(
        val name: String,
        val getter: () -> Boolean,
        val setter: (Boolean) -> Unit,
    )
}