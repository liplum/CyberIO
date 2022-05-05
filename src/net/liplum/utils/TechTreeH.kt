package net.liplum.utils

import arc.struct.ObjectMap
import arc.struct.Seq
import mindustry.content.TechTree
import mindustry.content.TechTree.TechNode
import mindustry.ctype.UnlockableContent
import mindustry.game.Objectives
import mindustry.type.ItemStack

fun Seq<TechNode>.withContext(
    func: Techtree.() -> Unit,
) {
    Techtree(this).func()
}
@JvmInline
value class Techtree(
    val all: Seq<TechNode>,
) {
    companion object {
        val map: ObjectMap<UnlockableContent, TechNode> =
            TechTree::class.java.getF("map")
    }

    fun at(content: UnlockableContent): TechNode =
        map[content]!!

    fun at(name: String): TechNode =
        TechTree.all.find { it.content.name == name }!!

    @JvmOverloads
    inline fun TechNode.sub(
        content: UnlockableContent,
        requirements: Array<ItemStack> = content.researchRequirements(),
        objectives: Seq<Objectives.Objective>? = null,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val node = TechNode(this, content, requirements)
        if (objectives != null) {
            node.objectives.addAll(objectives)
        }
        node.genChild()
        return node
    }
}
