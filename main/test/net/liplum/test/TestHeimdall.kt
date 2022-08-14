package net.liplum.test

import arc.math.Rand
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class TestHeimdall {
    @Test
    fun `test serialize random`() {
        val rand = Rand()
        println("A1:${rand.random(0, 100)}")
        println("A2:${rand.random(0, 100)}")
        println("A3:${rand.random(0, 100)}")
        var bytes: ByteArray
        run serialization@{
            val byteOut = ByteArrayOutputStream()
            val objOut = ObjectOutputStream(byteOut)
            objOut.writeObject(rand)
            objOut.flush()
            bytes = byteOut.toByteArray()
        }
        var randRestored: Rand
        run deserialization@{
            val byteIn = ByteArrayInputStream(bytes)
            val objIn = ObjectInputStream(byteIn)
            randRestored = objIn.readObject() as Rand
        }
        println()
        val b4 = printOut("B4", randRestored.random(0, 100))
        val b5 = printOut("B5", randRestored.random(0, 100))
        val b6 = printOut("B6", randRestored.random(0, 100))
        println()
        val a4 = printOut("A4", rand.random(0, 100))
        val a5 = printOut("A5", rand.random(0, 100))
        val a6 = printOut("A6", rand.random(0, 100))
        println()

        assert(b4 == a4)
        assert(b5 == a5)
        assert(b6 == a6)
    }

    fun printOut(id: String, value: Int): Int {
        println("$id:$value")
        return value
    }
}