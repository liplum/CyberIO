package net.liplum.lib.ui

interface INavigable {
    fun navigate(locator: INavigateLocator)
}

interface INavigationService {
    fun registerNavigable(navigable: INavigable)
    fun navigateGlobal(locator: INavigateLocator)
}

enum class NavigateKind {
    Relative, Global
}
/**
 * Tab1/Tab2
 */
interface INavigateLocator {
    val kind: NavigateKind
    val fragments: Iterable<String>
}

class NavigateLocator : INavigateLocator {
    override var kind = NavigateKind.Relative
    override val fragments: ArrayList<String> = ArrayList()
}
