package net.liplum.mdt

import arc.struct.Seq
import mindustry.content.TechTree
import mindustry.content.TechTree.TechNode
import mindustry.ctype.UnlockableContent
import mindustry.game.Objectives.Objective
import mindustry.game.Objectives.Research
import mindustry.type.ItemStack

inline fun Seq<TechNode>.withContext(
    func: Techtree.() -> Unit,
) {
    Techtree(this).func()
}

class NoSuchTechNodeException(msg: String) : RuntimeException(msg)
@JvmInline
value class Techtree(
    val all: Seq<TechNode>,
) {
    companion object {
        fun Array<out Any>.filterObjectives(): List<Objective> =
            this.filterIsInstance<UnlockableContent>().map {
                it.toObjective()
            } + this.filterIsInstance<Objective>()

        fun UnlockableContent.toObjective() =
            Research(this)
    }

    fun at(content: UnlockableContent): TechNode =
        content.techNode ?: throw NoSuchTechNodeException(content.name)

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
        vararg p: Any,
        overwriteReq: Boolean = false,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val req = p.filterIsInstance<ItemStack>()
        val reqStacks = p.filterIsInstance<Array<ItemStack>>()
        val allReq = (req + reqStacks.flatMap { it.toList() }).toMutableList()
        if (!overwriteReq)
            allReq += content.researchRequirements()
        val node = TechNode(this, content, allReq.toTypedArray())
        if (objectives != null) {
            val objectives = p.filterObjectives()
            node.objectives.addAll(objectives)
        }
        node.genChild()
        return node
    }
}
