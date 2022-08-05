package net.liplum.ui

import plumy.core.Out
import java.util.*

interface INavigable {
    val navigateFragment: String
    /**
     * Navigate by the [locator] in current scope.
     * @return whether this navigation succeeded.
     */
    fun navigate(locator: INavigator): Boolean
}

interface INavigationService : INavigable {
    fun registerNavigable(navigable: INavigable)
    /**
     * Navigation in [NavigateKind.Global].
     */
    override fun navigate(locator: INavigator): Boolean
}

enum class NavigateKind {
    Relative, Global
}
/**
 * ## Examples
 * ### Relative
 * - "./DialogA/Tab1" --> based on this
 * - "DialogB" --> based on this
 * - "../Tab1" --> go to parent (Not supported yet)
 * ### Global
 * - "/MainPage/Dialog"
 */
interface INavigator {
    val kind: NavigateKind
    val fragments: List<String>
}

class Navigator : INavigator {
    override var kind = NavigateKind.Relative
    override val fragments = LinkedList<String>()
    fun copyFrom(b: INavigator) {
        kind = b.kind
        fragments.clear()
        fragments.addAll(b.fragments)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (kind == NavigateKind.Global)
            sb.append('/')
        for ((i, frag) in fragments.withIndex()) {
            sb.append(frag)
            if (i < fragments.size - 1) {
                sb.append("/")
            }
        }
        return sb.toString()
    }

    companion object {
        fun by(text: String): Navigator =
            resolve(text, Navigator())

        fun Navigator.set(text: String): Navigator =
            resolve(text, this)
        /**
         * Resolve a navigation locator string.
         * @return the locator contains resolved fragments
         */
        fun resolve(text: String, @Out locator: Navigator): Navigator {
            locator.fragments.clear()
            locator.kind = NavigateKind.Relative
            val t = text.trim()
            if (t.isEmpty()) return locator
            val frags = t.split("/")
            if (frags[0].isEmpty()) {
                // It's global
                locator.kind = NavigateKind.Global
            }
            for (frag in frags) {
                // If the frag is empty or dot, skip this
                if (frag.isEmpty() || frag == ".") continue
                locator.fragments.addLast(frag)
            }
            return locator
        }
    }
}

class NavigationService : INavigationService {
    private val sharedLocator = Navigator()
    private val frag2Navigator = HashMap<String, INavigable>()
    override fun registerNavigable(navigable: INavigable) {
        frag2Navigator[navigable.navigateFragment] = navigable
    }

    override fun navigate(locator: INavigator): Boolean {
        // whatever the kind of locator, pretend it's global
        val frags = locator.fragments
        if (frags.isNotEmpty()) {
            val lv1 = frags.first()
            val navigable = frag2Navigator[lv1]
            if (navigable != null) {
                sharedLocator.copyFrom(locator)
                sharedLocator.fragments.removeFirst()
                sharedLocator.kind = NavigateKind.Relative
                return navigable.navigate(sharedLocator)
            }
        }
        return false
    }

    override val navigateFragment = "."
}

object EmptyNavigable : INavigable {
    override val navigateFragment = ""
    override fun navigate(locator: INavigator) = true
}