package net.liplum.processor.dp

import java.util.*

typealias ID = String

class DpGraph {
    private val id2Node = HashMap<ID, DpNode>()
    operator fun get(id: ID): DpNode =
        id2Node.getOrPut(id) { DpNode(id) }

    operator fun contains(id: ID): Boolean =
        id in id2Node

    private fun resolve(node: DpNode): LinkedList<DpNode> {
        val resolved = LinkedList<DpNode>()
        val unresolved = LinkedList<DpNode>()
        fun resolveFunc(cur: DpNode) {
            unresolved.add(cur)
            for (dependency in cur.dependencies) {
                if (dependency !in resolved) {
                    if (dependency in unresolved)
                        throw CircularDpException(cur, dependency)
                    resolveFunc(dependency)
                }
            }
            resolved.add(cur)
            unresolved.remove(cur)
        }
        resolveFunc(node)
        return resolved
    }
    /**
     * Resolve all dependencies in order.
     */
    fun resolveAllInOrder(): List<DpNode> {
        // step 1: find all leaf nodes
        val leaves = id2Node.values.filter { it.isDependent }
        val result = ArrayList<DpNode>()
        for (leaf in leaves) {
            // step 2: resolve all leaf nodes.
            val resolve = resolve(leaf)
            // step 3: add all into one list
            result.addAll(resolve)
        }
        // step 4: add all independent
        result.addAll(id2Node.values.filter { !it.isDependent })
        // step 5: remove all duplicate by id but keep the same order.
        return result.distinctBy { it.id }
    }

    inner class DpNode internal constructor(val id: String) {
        init {
            id2Node[id] = this
        }

        var isDependent = false
        val dependencies: MutableList<DpNode> = ArrayList()
        fun dependsOn(child: DpNode) {
            dependencies.add(child)
            isDependent = true
        }

        override fun toString() = id
    }
}

class CircularDpException(child: DpGraph.DpNode, parent: DpGraph.DpNode) :
    RuntimeException("Circular reference detected: ${child.id} -> ${parent.id}")
