package net.liplum.test

import net.liplum.ui.Navigator
import org.junit.jupiter.api.Test

class TestNavigation {
    @Test
    fun `test parse navigator`() {
        val split = "/abc/cbd/".split("/")
        println(split.size)
        println(split)

        println(Navigator.by("/abc/cbd/"))
        println(Navigator.by("abc/cbd/"))
    }
}