package net.liplum.mdt

import arc.struct.Seq
import arc.util.ArcRuntimeException
import mindustry.content.TechTree
import mindustry.content.TechTree.TechNode
import mindustry.ctype.UnlockableContent
import mindustry.game.Objectives.Objective
import mindustry.game.Objectives.Research
import mindustry.type.ItemStack

inline fun Seq<TechNode>.modify(
    config: TechtreeModification.() -> Unit,
) {
    TechtreeModification(this).config()
}

inline fun CreateTechTree(
    origin: UnlockableContent,
    name: String,
    requireUnlock: Boolean = false,
    config: TechTreeDeclaration.() -> Unit,
): TechNode {
    val root = TechNode(null, origin, origin.researchRequirements())
    root.name = name
    root.requiresUnlock = requireUnlock
    TechTree.roots.add(root)
    val declaration = TechTreeDeclaration(root)
    declaration.config()
    return root
}

class NoSuchTechNodeException(msg: String) : ArcRuntimeException(msg)
@JvmInline
value class TechtreeModification(
    val all: Seq<TechNode>,
) {
    fun at(
        content: UnlockableContent,
        genChild: TechNode.() -> Unit,
    ): TechNode {
        val cur = content.techNode ?: throw NoSuchTechNodeException(content.name)
        cur.genChild()
        return cur
    }

    fun at(
        name: String,
        genChild: TechNode.() -> Unit,
    ): TechNode {
        val cur = TechTree.all.find { it.content.name == name } ?: throw NoSuchTechNodeException(name)
        cur.genChild()
        return cur
    }

    inline fun TechNode.node(
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

    inline fun TechNode.node(
        content: UnlockableContent,
        vararg requirements: Any,
        overwriteReq: Boolean = false,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val req = requirements.filterIsInstance<ItemStack>()
        val reqStacks = requirements.filterIsInstance<Array<ItemStack>>()
        val allReq = (req + reqStacks.flatMap { it.toList() }).toMutableList()
        if (!overwriteReq)
            allReq += content.researchRequirements()
        val node = TechNode(this, content, allReq.toTypedArray())
        node.genChild()
        return node
    }
}
@JvmInline
value class TechTreeDeclaration(
    val root: TechNode,
) {
    inline fun node(
        content: UnlockableContent,
        requirements: Array<ItemStack> = content.researchRequirements(),
        objectives: Array<Objective>? = null,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val node = TechNode(root, content, requirements)
        if (objectives != null) {
            node.objectives.addAll(*objectives)
        }
        node.genChild()
        return node
    }

    inline fun node(
        content: UnlockableContent,
        vararg requirements: Any,
        overwriteReq: Boolean = false,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val req = requirements.filterIsInstance<ItemStack>()
        val reqStacks = requirements.filterIsInstance<Array<ItemStack>>()
        val allReq = (req + reqStacks.flatMap { it.toList() }).toMutableList()
        if (!overwriteReq)
            allReq += content.researchRequirements()
        val node = TechNode(root, content, allReq.toTypedArray())
        node.genChild()
        return node
    }

    inline fun TechNode.node(
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

    inline fun TechNode.node(
        content: UnlockableContent,
        vararg requirements: Any,
        overwriteReq: Boolean = false,
        genChild: TechNode.() -> Unit = {},
    ): TechNode {
        val req = requirements.filterIsInstance<ItemStack>()
        val reqStacks = requirements.filterIsInstance<Array<ItemStack>>()
        val allReq = (req + reqStacks.flatMap { it.toList() }).toMutableList()
        if (!overwriteReq)
            allReq += content.researchRequirements()
        val node = TechNode(this, content, allReq.toTypedArray())
        node.genChild()
        return node
    }
}

fun Array<out Any>.filterObjectives(): List<Objective> =
    this.filterIsInstance<UnlockableContent>().map {
        it.toObjective()
    } + this.filterIsInstance<Objective>()

fun UnlockableContent.toObjective() =
    Research(this)
