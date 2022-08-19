package net.liplum.blocks.decentralizer

import mindustry.Vars
import mindustry.type.ItemStack
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.consumers.ConsumeItems
import mindustry.world.consumers.ConsumePower
import net.liplum.CLog
import plumy.core.Out
import net.liplum.common.util.littleEndianByteB
import net.liplum.common.util.bigEndianByteB
import plumy.dsl.ID
import java.util.*

class Recipe(
    val ingredients: Array<ItemStack>,
    val output: Array<ItemStack>,
) {
    lateinit var id: BitSet
    var shortHash: Int = 0
    fun dst(b: Recipe) =
        HammingDistance.dst(this.id, b.id)

    override fun hashCode(): Int {
        return shortHash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        if (!ingredients.contentEquals(other.ingredients)) return false
        if (id != other.id) return false
        if (shortHash != other.shortHash) return false

        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$shortHash:")
        sb.append("(")
        for ((i, ing) in ingredients.withIndex()) {
            sb.append(ing.item.name)
            sb.append(",")
            sb.append(ing.amount)
            if (i < ingredients.size - 1)
                sb.append(",")
        }
        sb.append(")=>(")
        for ((i, out) in output.withIndex()) {
            sb.append(out.item.name)
            sb.append(",")
            sb.append(out.amount)
            if (i < output.size - 1)
                sb.append(",")
        }
        sb.append(")")
        return sb.toString()
    }

    companion object {
        @JvmField var MaxBits = 0
        val MaxBitSetSize: Int
            get() = MaxBits / 8
    }
}

object RecipeCenter {
    var AllRecipes: List<Recipe> = emptyList()
    @JvmStatic
    fun recordAllRecipes() {
        // Assume every item has 2 different ways to be synthesized
        val allRecipes = ArrayList<Recipe>(Vars.content.items().size * 2)
        doOnCrafter({ canBeDetected() }) {
            val itemConsumed = itemConsumed()
            if (itemConsumed != null) {
                val rec = Recipe(itemConsumed, outputItems)
                allRecipes.add(rec)
            }
        }
        if (allRecipes.isEmpty()) {
            CLog.warn("No recipe detected.")
            return
        }
        // 16 stands for id, 16 stands for amount
        Recipe.MaxBits = Vars.content.items().size * (16 + 16)
        allRecipes.forEach { it.calcuID() }
        AllRecipes = allRecipes.toList()
    }
    /**
     * Filling an array with full item id.
     * @receiver then add this into full item id array
     */
    fun Array<ItemStack>.toFull(): IntArray {
        val res = IntArray(Vars.content.items().size)
        for (stack in this) {
            res[stack.item.ID] = stack.amount
        }
        return res
    }

    fun GenericCrafter.canBeDetected(): Boolean {
        // It must output items
        if (outputItems == null) return false
        // It only consumes power or items
        if (consumers.filter {
                it is ConsumeItems || it is ConsumePower
            }.size != consumers.size) return false
        return true
    }
    @Out
    fun Recipe.calcuID(maxBit: Int = Recipe.MaxBits) {
        ingredients.sortBy { it.item.id }
        id = ingredients.toFull().calcuID(maxBit)
        shortHash = id.hashCode()
    }
    /**
     * Calculate the identity of ItemStack[]
     * be careful, this will raise [RuntimeException] when its *bit size* out of *max bits*
     * @receiver must be full id array
     */
    fun IntArray.calcuID(maxBit: Int = Recipe.MaxBits): BitSet {
        val bytes = ByteArray(maxBit / 8)
        for ((id, amount) in this.withIndex()) {
            try {
                bytes[id * 4] = id.littleEndianByteB
                bytes[id * 4 + 1] = id.bigEndianByteB
                bytes[id * 4 + 2] = amount.littleEndianByteB
                bytes[id * 4 + 3] = amount.bigEndianByteB
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw RuntimeException("Out of range: ${bytes.size}", e)
            }
        }
        return BitSet.valueOf(bytes)
    }

    fun Array<ItemStack>.bitSize() =
        // 16 stands for id, 16 stands for amount
        this.size * (16 + 16)

    fun GenericCrafter.itemConsumed(): Array<ItemStack>? {
        for (consumer in consumers) {
            if (consumer is ConsumeItems) {
                return consumer.items
            }
        }
        return null
    }

    inline fun doOnCrafter(
        filter: GenericCrafter.() -> Boolean,
        func: GenericCrafter.() -> Unit,
    ) {
        for (block in Vars.content.blocks()) {
            if (block is GenericCrafter)
                if (block.filter())
                    block.func()
        }
    }
}