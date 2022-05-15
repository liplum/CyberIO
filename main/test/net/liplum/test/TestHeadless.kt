package net.liplum.test

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

class TestHeadless {
    @Test
    @Tag("fast")
    fun `Find current execute folder`() {
        val a = File(
            TestHeadless::class.java.protectionDomain.codeSource.location
                .toURI()
        ).path
        println("current dir = $a")
        val b = System.getProperty("user.dir")
        println("current dir = $b")
    }
}