package net.liplum.blocks.decentralizer

import mindustry.Vars
import mindustry.type.ItemStack
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.consumers.ConsumeItems
import mindustry.world.consumers.ConsumePower
import net.liplum.Clog
import net.liplum.DebugOnly
import net.liplum.lib.Out
import net.liplum.lib.utils.shr
import java.util.*
import kotlin.experimental.and
import kotlin.math.max

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
        sb.append("Recipe(")
        sb.append("bits:$id")
        sb.append(",")
        sb.append("hash:$shortHash,")
        sb.append("(")
        for ((i,ing) in ingredients.withIndex()) {
            sb.append(ing.item.name)
            sb.append(",")
            sb.append(ing.amount)
            if(i < ingredients.size - 1)
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
        @JvmField var MaxIngredientSize = 0
    }
}

object RecipeCenter {
    @JvmField
    var Hash2Recipe: Map<Int, Recipe> = emptyMap()
    @JvmStatic
    fun recordAllRecipes() {
        var maxBitSize = 0
        var maxIngredientSize = 0
        // Assume every item has 2 different ways to be synthesized
        val allRecipes = ArrayList<Recipe>(Vars.content.items().size * 2)
        doOnCrafter({ canBeDetected() }) {
            val itemConsumed = itemConsumed()
            if (itemConsumed != null) {
                val rec = Recipe(itemConsumed, outputItems)
                maxBitSize = max(maxBitSize, itemConsumed.bitSize())
                maxIngredientSize = max(maxIngredientSize, itemConsumed.size)
                allRecipes.add(rec)
            }
        }
        if (maxBitSize == 0 || allRecipes.isEmpty()) {
            Clog.warn("No recipe detected.")
            return
        }
        Recipe.MaxBits = maxBitSize
        Recipe.MaxIngredientSize = maxIngredientSize
        allRecipes.forEach { it.calcuID() }
        Hash2Recipe = allRecipes.associateBy { it.id.hashCode() }
        DebugOnly {
            val difference = allRecipes - Hash2Recipe.values.toSet()
            for (recipe in difference) {
                Clog.info(recipe)
            }
        }
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
        id = ingredients.calcuID(maxBit)
        shortHash = id.hashCode()
    }
    /**
     * Calculate the identity of ItemStack[]
     * be careful, this will raise [RuntimeException] when its *bit size* out of *max bits*
     */
    fun Array<ItemStack>.calcuID(maxBit: Int = Recipe.MaxBits): BitSet {
        val bytes = ByteArray(maxBit / 8)
        for ((i, stack) in this.withIndex()) {
            val itemID = stack.item.id
            val amount = stack.amount.toShort()
            try {
                bytes[i * 2] = (itemID and 0xff).toByte()
                bytes[i * 2 + 1] = ((itemID shr 8) and 0xff).toByte()
                bytes[i * 2 + 2] = (amount and 0xff).toByte()
                bytes[i * 2 + 3] = ((amount shr 8) and 0xff).toByte()
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
        func: GenericCrafter.() -> Unit
    ) {
        for (block in Vars.content.blocks()) {
            if (block is GenericCrafter)
                if (block.filter())
                    block.func()
        }
    }
}