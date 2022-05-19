@file:Suppress("MoveLambdaOutsideParentheses")

package net.liplum

object Debug {
    var enableUnlockContent = false
    var settings = listOf(
        Setting(
            "Enable Unlock Content",
            ::enableUnlockContent::get,
            { enableUnlockContent = it as? Boolean ?: false },
        )
    )

    class Setting(
        val name: String,
        val getter: () -> Any,
        val setter: (Any) -> Unit,
    )
}