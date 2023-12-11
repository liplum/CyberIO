package net.liplum

import net.liplum.processor.dp.DpGraph
import org.junit.jupiter.api.Test

class TestDpResolution {
    @Test
    fun `Test resolution all dependencies`() {
        val dpGraph = DpGraph()
        // init
        val a = dpGraph["a"]
        val b = dpGraph["b"]
        val c = dpGraph["c"]
        val d = dpGraph["d"]
        val e = dpGraph["e"]
        // add dependencies
        a.dependsOn(b)
        a.dependsOn(d)
        b.dependsOn(c)
        b.dependsOn(e)
        c.dependsOn(d)
        c.dependsOn(e)
        // resolve
        val resolution = dpGraph.resolveAllInOrder()
        println(resolution)
    }
}