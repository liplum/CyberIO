package net.liplum.common.delegate

class Args {
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