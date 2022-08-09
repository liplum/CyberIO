package net.liplum.common.util

inline fun DoMultipleBool(isMultiple: Boolean, times: Int, action: () -> Boolean): Boolean {
    return if (isMultiple) {
        var did = false
        for (i in 0 until times) {
            if (action()) {
                did = true
            }
        }
        did
    } else
        action()
}

inline fun DoMultipleBool(times: Int, action: () -> Boolean): Boolean {
    var did = false
    for (i in 0 until times) {
        if (action()) {
            did = true
        }
    }
    return did
}

inline fun DoMultiple(isMultiple: Boolean, times: Int, action: () -> Unit) =
    if (isMultiple)
        for (i in 0 until times)
            action()
    else
        action()

inline fun DoMultiple(times: Int, action: () -> Unit) {
    for (i in 0 until times)
        action()
}
