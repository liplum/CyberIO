package net.liplum.lib

import arc.struct.ObjectMap
import arc.struct.Seq
import mindustry.content.TechTree
import mindustry.content.TechTree.TechNode
import mindustry.ctype.UnlockableContent
import mindustry.game.Objectives.Objective
import mindustry.game.Objectives.Research
import mindustry.type.ItemStack
import net.liplum.UseReflection

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
        @UseReflection
        val map: ObjectMap<UnlockableContent, TechNode> =
            TechTree::class.java.getF("map")

        fun Array<out Any>.filterObjectives(): List<Objective> =
            this.filterIsInstance<UnlockableContent>().map {
                it.toObjective()
            } + this.filterIsInstance<Objective>()

        fun UnlockableContent.toObjective() =
            Research(this)
    }

    fun at(content: UnlockableContent): TechNode =
        map[content]!!

    fun at(name: String): TechNode =
        TechTree.all.find { it.content.name == name }!!
    @JvmOverloads
    inline fun TechNode.sub(
        content: UnlockableContent,
        requirements: Array<ItemStack> = content.researchRequirements(),
        objectives: Array<Objective>? = null,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val node = TechNode(this, content, requirements)
        if (objectives != null) {
            node.objectives.addAll(*objectives)
        }
        node.genChild()
        return node
    }
    @JvmOverloads
    inline fun TechNode.sub(
        content: UnlockableContent,
        vararg args: Any,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val req = args.filterIsInstance<ItemStack>().toTypedArray()
        val node = TechNode(this, content, req)
        if (objectives != null) {
            val objectives = args.filterObjectives()
            node.objectives.addAll(objectives)
        }
        node.genChild()
        return node
    }
}
