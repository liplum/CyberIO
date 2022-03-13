package net.liplum.delegates

open class Args {
    var data: Any? = null
        private set

    private constructor()
    constructor(data: Any) {
        this.data = data
    }

    companion object {
        private val Empty = Args()
        @JvmStatic
        fun empty(): Args = Empty
    }
}